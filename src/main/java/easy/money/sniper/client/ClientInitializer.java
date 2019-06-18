package easy.money.sniper.client;

import easy.money.sniper.model.RPCRequest;
import easy.money.sniper.model.RPCResponse;
import easy.money.sniper.serialization.ProtobufDecoder;
import easy.money.sniper.serialization.ProtobufEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 14:15
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new ProtobufEncoder(RPCRequest.class))
                .addLast(new ProtobufDecoder(RPCResponse.class))
                .addLast(new ClientHandler());
    }
}
