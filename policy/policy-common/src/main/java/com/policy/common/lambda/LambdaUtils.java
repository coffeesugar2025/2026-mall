package com.policy.common.lambda;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.SerializationUtils;


public class LambdaUtils {
    private static final Map<Class<?>, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();


    public static <T, R> String get(SFunction<T, R> func, boolean onlyColumn) {
        String key = getSerializedLambda(func).getImplMethodName();
        if (!onlyColumn) {
            return key;
        }
        if (key.startsWith("get")) {
            key = key.substring(3);
        } else if (key.startsWith("is")) {
            key = key.substring(2);
        }
        return key.substring(0, 1).toLowerCase() + key.substring(1);
    }

    public static <T, R> String get(SFunction<T, R> func) {
        return get(func, true);
    }


    public static <T, R> SerializedLambda getSerializedLambda(SFunction<T, R> func) {
        Class<?> clazz = func.getClass();

        return Optional.ofNullable(FUNC_CACHE.get(clazz)).map(Reference::get).orElseGet(() -> {
            SerializedLambda lambda = resolveProcess(func);
            FUNC_CACHE.put(clazz, new WeakReference<>(lambda));
            return lambda;
        });
    }


    private static <T> SerializedLambda resolveProcess(SFunction<T, ?> func) {
        if (!func.getClass().isSynthetic()) {
            throw new RuntimeException("not lambda synthetic");
        }
        byte[] serialize = SerializationUtils.serialize(func);
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(Objects.<byte[]>requireNonNull(serialize))) {
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz = super.resolveClass(objectStreamClass);
                return (clazz == SerializedLambda.class) ? SerializedLambda.class : clazz;
            }
        }) {
            return (SerializedLambda) objIn.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("This is impossible to happen", e);
        }
    }
}
