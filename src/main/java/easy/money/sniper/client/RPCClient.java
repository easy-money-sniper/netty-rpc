package easy.money.sniper.client;

import easy.money.sniper.server.RPCServer;

import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 11:04
 */
public class RPCClient {

    private static ThreadPoolExecutor executor;

    @SuppressWarnings("unchecked")
    public static <T> T getClient(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new ClientProxy());
    }

    public static AsyncProxy getAsyncClient(Class<?> interfaceClass) {
        return new ClientProxy(interfaceClass);
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
}
