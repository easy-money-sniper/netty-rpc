package easy.money.sniper.client;

import easy.money.sniper.model.RPCFuture;
import easy.money.sniper.model.RPCRequest;
import easy.money.sniper.model.RPCResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/17 14:16
 */
public class ClientHandler extends SimpleChannelInboundHandler<RPCResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
    private volatile Channel channel;
    private Map<String, RPCFuture> pendingFuture;

    public ClientHandler() {
        this.pendingFuture = new ConcurrentHashMap<>();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RPCResponse response) throws Exception {
        RPCFuture future = pendingFuture.get(response.getRequestId());

        if (future == null) {
            return;
        }

        pendingFuture.remove(response.getRequestId());

        future.done(response);
    }

    public RPCFuture sendRequest(RPCRequest request) {
        RPCFuture future = new RPCFuture(request);

        pendingFuture.put(request.getRequestId(), future);

        channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
            LOGGER.info("发送请求成功，request={}", request);
        });

        return future;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public SocketAddress getRemoteAddress() {
        return Optional.ofNullable(channel).map(Channel::remoteAddress).orElse(null);
    }
}