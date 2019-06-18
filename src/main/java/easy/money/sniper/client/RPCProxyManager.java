package easy.money.sniper.client;

import java.lang.reflect.Proxy;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 11:04
 */
public class RPCProxyManager {

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
}
