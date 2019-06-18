package easy.money.sniper.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 15:52
 * 基于Google Protostuff序列化
 */
public class ProtobufEncoder extends MessageToByteEncoder {

    private final Class<?> clazz;

    public ProtobufEncoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        // 传输对象不是预期的类型
        if (!clazz.isInstance(o)) {
            return;
        }

        byte[] bytes = SerializationUtil.serialize(o);

        // 先写数据长度
        byteBuf.writeInt(bytes.length);
        // 后写数据
        byteBuf.writeBytes(bytes);
    }
}
