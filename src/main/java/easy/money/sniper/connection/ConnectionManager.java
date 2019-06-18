package easy.money.sniper.connection;


import easy.money.sniper.client.ClientHandler;
import easy.money.sniper.client.ClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 11:33
 */
public class ConnectionManager {
    // 连接轮询策略
    private AtomicInteger roundRobbin = new AtomicInteger();

    private List<ClientHandler> connectedHandlers;
    private Map<SocketAddress, ClientHandler> connectedHandlerMap;

    private volatile boolean running;
    private ThreadPoolExecutor executor;
    private Lock lock;
    private Condition condition;

    private ConnectionManager() {
        this.connectedHandlers = new CopyOnWriteArrayList<>();
        this.connectedHandlerMap = new ConcurrentHashMap<>();
        this.running = true;
        this.executor = new ThreadPoolExecutor(
                5,
                5,
                600,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(65535));
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    public static ConnectionManager getInstance() {
        return ConnectionManagerHolder.CONNECTION_MANAGER;
    }

    /**
     * 选择连接 round robbin
     *
     * @return ClientHandler
     */
    public ClientHandler choose() {
        int size = connectedHandlers.size();

        while (running && size == 0) {
            try {
                waitForConnecting();
                size = connectedHandlers.size();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int index = roundRobbin.getAndIncrement() % size;

        return connectedHandlers.get(index);
    }

    /**
     * 已有新连接，通知线程获取连接处理
     */
    private void signalAll() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 等待新连接建立
     *
     * @throws InterruptedException 中断异常
     */
    private void waitForConnecting() throws InterruptedException {
        lock.lock();
        try {
            condition.await();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新新节点：新增新连接，移除不存在的连接
     *
     * @param newNodes 最新的所有节点信息
     */
    public void updateServerNodes(List<String> newNodes) {
        if (CollectionUtils.isEmpty(newNodes)) {
            connectedHandlers.forEach(clientHandler -> {
                clientHandler.close();
                connectedHandlerMap.remove(clientHandler.getRemoteAddress());
            });
            connectedHandlers.clear();
            return;
        }

        // 连接新节点
        Set<SocketAddress> newSet = new HashSet<>();
        String[] temp;
        for (String address : newNodes) {
            temp = address.split(":");
            SocketAddress socketAddress = new InetSocketAddress(temp[0], Integer.parseInt(temp[1]));
            newSet.add(socketAddress);

            if (connectedHandlerMap.containsKey(socketAddress)) {
                continue;
            }
            connectNode(address);
        }

        // 移除旧连接
        for (ClientHandler handler : connectedHandlers) {
            if (!newSet.contains(handler.getRemoteAddress())) {
                handler.close();
                connectedHandlers.remove(handler);
                connectedHandlerMap.remove(handler.getRemoteAddress());
            }
        }
    }

    /**
     * 建立长连接并保存
     *
     * @param address 服务地址
     */
    private void connectNode(String address) {
        executor.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();
            EventLoopGroup group = new NioEventLoopGroup();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientInitializer());

            String[] hostAndPort = address.split(":");
            bootstrap.connect(hostAndPort[0], Integer.parseInt(hostAndPort[1]))
                    .addListener((ChannelFutureListener) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            ClientHandler clientHandler = channelFuture.channel().pipeline().get(ClientHandler.class);
                            // 保存长链接
                            connectedHandlers.add(clientHandler);
                            connectedHandlerMap.put(clientHandler.getRemoteAddress(), clientHandler);
                            signalAll();
                        }
                    });
        });
    }

    private static class ConnectionManagerHolder {
        private static final ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
    }
}
