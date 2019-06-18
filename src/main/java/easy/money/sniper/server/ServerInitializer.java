package easy.money.sniper.server;

import easy.money.sniper.model.RPCRequest;
import easy.money.sniper.model.RPCResponse;
import easy.money.sniper.serialization.ProtobufDecoder;
import easy.money.sniper.serialization.ProtobufEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 16:27
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final Map<String, Object> handlerMap;

    public ServerInitializer(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new ProtobufDecoder(RPCRequest.class))
                .addLast(new ProtobufEncoder(RPCResponse.class))
                .addLast(new ServerHandler(handlerMap));
    }
}
