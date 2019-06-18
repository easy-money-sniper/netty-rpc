package easy.money.sniper.server;

import easy.money.sniper.model.RPCRequest;
import easy.money.sniper.model.RPCResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 16:43
 */
public class ServerHandler extends SimpleChannelInboundHandler<RPCRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
    private final Map<String, Object> handlerMap;

    public ServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCRequest request) throws Exception {
        RPCServer.submit(() -> {
            RPCResponse response = new RPCResponse();
            response.setServerStartTime(Instant.now().toEpochMilli());
            response.setRequestId(request.getRequestId());
            Object res = null;
            try {
                if (Math.random() < 0.5f) {
                    throw new Exception();
                }
                res = handleRequest(request);
            } catch (Exception e) {
                response.setError(e);
            }
            response.setServerEndTime(Instant.now().toEpochMilli());
            response.setResult(res);

            ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture ->
                    LOGGER.info("处理请求完成，request={}，response={}", request, response, response.getError()));
        });
    }

    private Object handleRequest(RPCRequest request) throws Exception {
        String className = request.getClassName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Object serviceBean = handlerMap.get(className);

        if (serviceBean == null) {
            throw new Exception("未发现接口" + className + "的处理器实例");
        }

        // 模拟处理响应时间
        // TimeUnit.MILLISECONDS.sleep(100);

        // Cglib
        FastClass fastClass = FastClass.create(serviceBean.getClass());

        int methodIndex = fastClass.getIndex(methodName, parameterTypes);

        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }
}
