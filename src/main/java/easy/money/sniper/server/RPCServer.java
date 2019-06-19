package easy.money.sniper.server;

import easy.money.sniper.common.Const;
import easy.money.sniper.registry.ServiceRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 15:52
 */
public class RPCServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCServer.class);
    private volatile static ThreadPoolExecutor executor;
    private final ServiceRegister registry;
    private final String serverAddress;
    private final Map<String, Object> handlerMap;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public RPCServer(String serverAddress) {
        this(serverAddress, Const.DEFAULT_ZK_ADDRESS);
    }

    public RPCServer(String serverAddress, String zkAddress) {
        this.registry = new ServiceRegister("127.0.0.1:9576", zkAddress);
        this.serverAddress = serverAddress;
        this.handlerMap = new HashMap<>();
        registry.start();
    }

    public static void submit(Runnable runnable) {
        if (executor == null) {
            synchronized (RPCServer.class) {
                if (null == executor) {
                    executor = new ThreadPoolExecutor(
                            300,
                            300,
                            60,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(),
                            new ThreadPoolExecutor.DiscardOldestPolicy()
                    );
                }
            }
        }

        executor.submit(runnable);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * Netty服务启动
     *
     * @throws Exception exception
     */
    public void start() throws Exception {
        if (null == boss && null == worker) {
            ServerBootstrap bootstrap = new ServerBootstrap();
            boss = new NioEventLoopGroup(1);
            worker = new NioEventLoopGroup();

            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ServerInitializer(handlerMap));

            String[] hostAndPort = serverAddress.split(":");

            ChannelFuture future = bootstrap.bind(hostAndPort[0], Integer.parseInt(hostAndPort[1]));

            // 注册服务
            boolean success = registry.registerService();
            if (!success) {
                throw new IllegalStateException("注册服务失败：" + serverAddress);
            }

            LOGGER.info("服务启动并注册成功，{}", serverAddress);

            future.channel().closeFuture().sync();
        }
    }

    /**
     * 服务关闭
     */
    public void stop() {
        if (null != registry) {
            registry.stop();
        }

        if (null != boss) {
            boss.shutdownGracefully();
        }

        if (null != worker) {
            worker.shutdownGracefully();
        }
        LOGGER.info("服务关闭成功");
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(RPCService.class);

        if (serviceBeans.isEmpty()) {
            throw new NoSuchBeanDefinitionException(RPCService.class);
        }

        for (Object bean : serviceBeans.values()) {
            Class<?> clazz = bean.getClass().getAnnotation(RPCService.class).value();
            String className = clazz.getName();

            if (handlerMap.containsKey(className)) {
                throw new IllegalStateException(className + "存在多个实例");
            }

            handlerMap.put(className, bean);
        }
    }
}
