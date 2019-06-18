package easy.money.sniper.serialization;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 15:53
 * 基于Google Protostuff反序列化
 */
public class ProtobufDecoder extends ByteToMessageDecoder {

    private final Class<?> clazz;

    public ProtobufDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 数据长度还没写入
        if (byteBuf.readableBytes() < 4) {
            return;
        }

        // 先读取数据长度信息
        byteBuf.markReaderIndex();
        int length = byteBuf.readInt();

        // 若数据未达到预期长度，重置
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        list.add(SerializationUtil.deserialize(bytes, clazz));
    }
}
