package easy.money.sniper.client;

import easy.money.sniper.connection.ConnectionManager;
import easy.money.sniper.model.RPCFuture;
import easy.money.sniper.model.RPCRequest;
import easy.money.sniper.model.RPCResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 11:04
 */
public class ClientProxy implements InvocationHandler, AsyncProxy {
    private Class<?> clazz;

    public ClientProxy() {
    }

    public ClientProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RPCRequest request = new RPCRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        // 选择连接
        ClientHandler handler = ConnectionManager.getInstance().choose();

        long startTime = Instant.now().toEpochMilli();
        // 发送请求
        RPCFuture future = handler.sendRequest(request);

        RPCResponse response = future.get(500, TimeUnit.MILLISECONDS);
        response.setClientStartTime(startTime);

        return response.getResult();
    }

    @Override
    public RPCFuture call(String methodName, Object... args) {
        RPCRequest request = new RPCRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(clazz.getName());
        request.setMethodName(methodName);
        request.setParameters(args);

        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0, len = args.length; i < len; i++) {
            parameterTypes[i] = args[i].getClass();
        }

        request.setParameterTypes(parameterTypes);

        ClientHandler handler = ConnectionManager.getInstance().choose();

        return handler.sendRequest(request);
    }
}
