package easy.money.sniper.serialization;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Liang Xu E-Mail: xuliang0706@gmail.com Date: 2019/06/12 15:53
 * <p>
 * refer: https://github.com/protostuff/protostuff
 * TODO: 支持多种序列化方法
 */
public class SerializationUtil {

    // protostuff schema
    private static final Map<Class, Schema> PROTO_SCHEMA_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();

        Schema<T> schema = getSchema(clazz);

        LinkedBuffer buffer = LinkedBuffer.allocate();

        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] protostuff, Class<T> clazz) {
        Schema<T> schema = getSchema(clazz);

        T obj = schema.newMessage();

        ProtostuffIOUtil.mergeFrom(protostuff, obj, schema);

        return obj;
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        return (Schema<T>) PROTO_SCHEMA_MAP.computeIfAbsent(clazz, RuntimeSchema::getSchema);
    }
}
