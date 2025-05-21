//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.flink.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nullable;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.io.IOReadableWritable;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataInputViewStreamWrapper;
import org.apache.flink.core.memory.DataOutputViewStreamWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public final class InstantiationUtil {
    private static final Logger LOG = LoggerFactory.getLogger(InstantiationUtil.class);
    private static final VersionMismatchHandler versionMismatchHandler = new VersionMismatchHandler();

    public static <T> T instantiate(String className, Class<T> targetType, ClassLoader classLoader) throws FlinkException {
        Class clazz;
        try {
            clazz = Class.forName(className, false, classLoader).asSubclass(targetType);
        } catch (ClassNotFoundException var5) {
            ClassNotFoundException e = var5;
            throw new FlinkException(String.format("Could not instantiate class '%s' of type '%s'. Please make sure that this class is on your class path.", className, targetType.getName()), e);
        }

        return (T) instantiate(clazz);
    }

    public static <T> T instantiate(Class<T> clazz, Class<? super T> castTo) {
        if (clazz == null) {
            throw new NullPointerException();
        } else if (castTo != null && !castTo.isAssignableFrom(clazz)) {
            throw new RuntimeException("The class '" + clazz.getName() + "' is not a subclass of '" + castTo.getName() + "' as is required.");
        } else {
            return instantiate(clazz);
        }
    }

    public static <T> T instantiate(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        } else {
            try {
                return clazz.newInstance();
            } catch (IllegalAccessException | InstantiationException var3) {
                ReflectiveOperationException iex = var3;
                checkForInstantiation(clazz);
                throw new RuntimeException("Could not instantiate type '" + clazz.getName() + "' due to an unspecified exception: " + iex.getMessage(), iex);
            } catch (Throwable var4) {
                Throwable t = var4;
                String message = t.getMessage();
                throw new RuntimeException("Could not instantiate type '" + clazz.getName() + "' Most likely the constructor (or a member variable initialization) threw an exception" + (message == null ? "." : ": " + message), t);
            }
        }
    }

    public static boolean hasPublicNullaryConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getConstructors()).anyMatch((constructor) -> {
            return constructor.getParameterCount() == 0;
        });
    }

    public static boolean isPublic(Class<?> clazz) {
        return Modifier.isPublic(clazz.getModifiers());
    }

    public static boolean isProperClass(Class<?> clazz) {
        int mods = clazz.getModifiers();
        return !Modifier.isAbstract(mods) && !Modifier.isInterface(mods) && !Modifier.isNative(mods);
    }

    public static boolean isNonStaticInnerClass(Class<?> clazz) {
        return clazz.getEnclosingClass() != null && (clazz.getDeclaringClass() == null || !Modifier.isStatic(clazz.getModifiers()));
    }

    public static void checkForInstantiation(Class<?> clazz) {
        String errorMessage = checkForInstantiationError(clazz);
        if (errorMessage != null) {
            throw new RuntimeException("The class '" + clazz.getName() + "' is not instantiable: " + errorMessage);
        }
    }

    public static String checkForInstantiationError(Class<?> clazz) {
        if (!isPublic(clazz)) {
            return "The class is not public.";
        } else if (clazz.isArray()) {
            return "The class is an array. An array cannot be simply instantiated, as with a parameterless constructor.";
        } else if (!isProperClass(clazz)) {
            return "The class is not a proper class. It is either abstract, an interface, or a primitive type.";
        } else if (isNonStaticInnerClass(clazz)) {
            return "The class is an inner class, but not statically accessible.";
        } else {
            return !hasPublicNullaryConstructor(clazz) ? "The class has no (implicit) public nullary constructor, i.e. a constructor without arguments." : null;
        }
    }

    @Nullable
    public static <T> T readObjectFromConfig(Configuration config, String key, ClassLoader cl) throws IOException, ClassNotFoundException {
        byte[] bytes = config.getBytes(key, (byte[]) null);
        return bytes == null ? null : deserializeObject(bytes, cl);
    }

    public static void writeObjectToConfig(Object o, Configuration config, String key) throws IOException {
        byte[] bytes = serializeObject(o);
        config.setBytes(key, bytes);
    }

    public static <T> byte[] serializeToByteArray(TypeSerializer<T> serializer, T record) throws IOException {
        if (record == null) {
            throw new NullPointerException("Record to serialize to byte array must not be null.");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(64);
            DataOutputViewStreamWrapper outputViewWrapper = new DataOutputViewStreamWrapper(bos);
            serializer.serialize(record, outputViewWrapper);
            return bos.toByteArray();
        }
    }

    public static <T> T deserializeFromByteArray(TypeSerializer<T> serializer, byte[] buf) throws IOException {
        if (buf == null) {
            throw new NullPointerException("Byte array to deserialize from must not be null.");
        } else {
            DataInputViewStreamWrapper inputViewWrapper = new DataInputViewStreamWrapper(new ByteArrayInputStream(buf));
            return serializer.deserialize(inputViewWrapper);
        }
    }

    public static <T> T deserializeFromByteArray(TypeSerializer<T> serializer, T reuse, byte[] buf) throws IOException {
        if (buf == null) {
            throw new NullPointerException("Byte array to deserialize from must not be null.");
        } else {
            DataInputViewStreamWrapper inputViewWrapper = new DataInputViewStreamWrapper(new ByteArrayInputStream(buf));
            return serializer.deserialize(reuse, inputViewWrapper);
        }
    }

    public static <T> T deserializeObject(byte[] bytes, ClassLoader cl) throws IOException, ClassNotFoundException {
        return deserializeObject((InputStream) (new ByteArrayInputStream(bytes)), cl);
    }

    public static <T> T deserializeObject(InputStream in, ClassLoader cl) throws IOException, ClassNotFoundException {
        return deserializeObject(in, cl, false);
    }

    public static <T> T deserializeObject(InputStream in, ClassLoader cl, boolean tolerateKnownVersionMismatch) throws IOException, ClassNotFoundException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();

        Object var5;
        try {
            ObjectInputStream oois = tolerateKnownVersionMismatch ? new FailureTolerantObjectInputStream(in, cl) : new ClassLoaderObjectInputStream(in, cl);
            Thread.currentThread().setContextClassLoader(cl);
            var5 = ((ObjectInputStream) oois).readObject();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        return (T) var5;
    }

    public static <T> T decompressAndDeserializeObject(byte[] bytes, ClassLoader cl) throws IOException, ClassNotFoundException {
        return deserializeObject((InputStream) (new InflaterInputStream(new ByteArrayInputStream(bytes))), cl);
    }

    public static byte[] serializeObject(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Throwable var2 = null;

        Object var5;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            Throwable var4 = null;

            try {
                oos.writeObject(o);
                oos.flush();
                var5 = baos.toByteArray();
            } catch (Throwable var28) {
                var5 = var28;
                var4 = var28;
                throw var28;
            } finally {
                if (oos != null) {
                    if (var4 != null) {
                        try {
                            oos.close();
                        } catch (Throwable var27) {
                            var4.addSuppressed(var27);
                        }
                    } else {
                        oos.close();
                    }
                }

            }
        } catch (Throwable var30) {
            var2 = var30;
            throw var30;
        } finally {
            if (baos != null) {
                if (var2 != null) {
                    try {
                        baos.close();
                    } catch (Throwable var26) {
                        var2.addSuppressed(var26);
                    }
                } else {
                    baos.close();
                }
            }

        }

        return (byte[]) var5;
    }

    public static void serializeObject(OutputStream out, Object o) throws IOException {
        ObjectOutputStream oos = out instanceof ObjectOutputStream ? (ObjectOutputStream) out : new ObjectOutputStream(out);
        oos.writeObject(o);
    }

    public static byte[] serializeObjectAndCompress(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Throwable var2 = null;

        Object var7;
        try {
            DeflaterOutputStream dos = new DeflaterOutputStream(baos);
            Throwable var4 = null;

            try {
                ObjectOutputStream oos = new ObjectOutputStream(dos);
                Throwable var6 = null;

                try {
                    oos.writeObject(o);
                    oos.flush();
                    dos.close();
                    var7 = baos.toByteArray();
                } catch (Throwable var51) {
                    var7 = var51;
                    var6 = var51;
                    throw var51;
                } finally {
                    if (oos != null) {
                        if (var6 != null) {
                            try {
                                oos.close();
                            } catch (Throwable var50) {
                                var6.addSuppressed(var50);
                            }
                        } else {
                            oos.close();
                        }
                    }

                }
            } catch (Throwable var53) {
                var4 = var53;
                throw var53;
            } finally {
                if (dos != null) {
                    if (var4 != null) {
                        try {
                            dos.close();
                        } catch (Throwable var49) {
                            var4.addSuppressed(var49);
                        }
                    } else {
                        dos.close();
                    }
                }

            }
        } catch (Throwable var55) {
            var2 = var55;
            throw var55;
        } finally {
            if (baos != null) {
                if (var2 != null) {
                    try {
                        baos.close();
                    } catch (Throwable var48) {
                        var2.addSuppressed(var48);
                    }
                } else {
                    baos.close();
                }
            }

        }

        return (byte[]) var7;
    }

    public static boolean isSerializable(Object o) {
        try {
            serializeObject(o);
            return true;
        } catch (IOException var2) {
            return false;
        }
    }

    public static <T extends Serializable> T clone(T obj) throws IOException, ClassNotFoundException {
        return obj == null ? null : clone(obj, obj.getClass().getClassLoader());
    }

    public static <T extends Serializable> T clone(T obj, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        if (obj == null) {
            return null;
        } else {
            byte[] serializedObject = serializeObject(obj);
            return (T) deserializeObject(serializedObject, classLoader);
        }
    }

    public static <T extends Serializable> T cloneUnchecked(T obj) {
        try {
            return clone(obj, obj.getClass().getClassLoader());
        } catch (ClassNotFoundException | IOException var2) {
            Exception e = var2;
            throw new RuntimeException(String.format("Unable to clone instance of %s.", obj.getClass().getName()), e);
        }
    }

    public static <T extends IOReadableWritable> T createCopyWritable(T original) throws IOException {
        if (original == null) {
            return null;
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputViewStreamWrapper out = new DataOutputViewStreamWrapper(baos);
            Throwable var3 = null;

            try {
                original.write(out);
            } catch (Throwable var28) {
                var3 = var28;
                throw var28;
            } finally {
                if (out != null) {
                    if (var3 != null) {
                        try {
                            out.close();
                        } catch (Throwable var26) {
                            var3.addSuppressed(var26);
                        }
                    } else {
                        out.close();
                    }
                }

            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            DataInputViewStreamWrapper in = new DataInputViewStreamWrapper(bais);
            Throwable var4 = null;

            IOReadableWritable var6;
            try {
                T copy = (T) instantiate(original.getClass());
                copy.read(in);
                var6 = copy;
            } catch (Throwable var27) {
                var4 = var27;
                throw var27;
            } finally {
                if (in != null) {
                    if (var4 != null) {
                        try {
                            in.close();
                        } catch (Throwable var25) {
                            var4.addSuppressed(var25);
                        }
                    } else {
                        in.close();
                    }
                }

            }

            return (T) var6;
        }
    }

    public static <T> Class<T> resolveClassByName(DataInputView in, ClassLoader cl) throws IOException {
        return resolveClassByName(in, cl, Object.class);
    }

    public static <T> Class<T> resolveClassByName(DataInputView in, ClassLoader cl, Class<? super T> supertype) throws IOException {
        String className = in.readUTF();

        Class rawClazz;
        try {
            rawClazz = Class.forName(className, false, cl);
        } catch (ClassNotFoundException var7) {
            ClassNotFoundException e = var7;
            String error = "Could not find class '" + className + "' in classpath.";
            if (className.contains("SerializerConfig")) {
                error = error + " TypeSerializerConfigSnapshot and it's subclasses are not supported since Flink 1.17. If you are using built-in serializers, please first migrate to Flink 1.16. If you are using custom serializers, please migrate them to TypeSerializerSnapshot using Flink 1.16.";
            }

            throw new IOException(error, e);
        }

        if (!supertype.isAssignableFrom(rawClazz)) {
            throw new IOException("The class " + className + " is not a subclass of " + supertype.getName());
        } else {
            Class<T> clazz = rawClazz;
            return clazz;
        }
    }

    private InstantiationUtil() {
        throw new RuntimeException();
    }

    static {
        versionMismatchHandler.addVersionsMatch("org.apache.flink.table.runtime.typeutils.MapDataSerializer", 4073842523628732956L, Collections.singletonList(2533002123505507000L));
    }

    public static class FailureTolerantObjectInputStream extends ClassLoaderObjectInputStream {
        public FailureTolerantObjectInputStream(InputStream in, ClassLoader cl) throws IOException {
            super(in, cl);
        }

        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass streamClassDescriptor = super.readClassDescriptor();
            Class localClass = this.resolveClass(streamClassDescriptor);
            String name = localClass.getName();
            if (InstantiationUtil.versionMismatchHandler.haveRulesForClass(name)) {
                ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
                if (localClassDescriptor != null && localClassDescriptor.getSerialVersionUID() != streamClassDescriptor.getSerialVersionUID() && InstantiationUtil.versionMismatchHandler.shouldTolerateSerialVersionMismatch(name, localClassDescriptor.getSerialVersionUID(), streamClassDescriptor.getSerialVersionUID())) {
                    InstantiationUtil.LOG.warn("Ignoring serialVersionUID mismatch for class {}; was {}, now {}.", new Object[]{streamClassDescriptor.getName(), streamClassDescriptor.getSerialVersionUID(), localClassDescriptor.getSerialVersionUID()});
                    streamClassDescriptor = localClassDescriptor;
                }
            }

            return streamClassDescriptor;
        }
    }

    private static final class VersionMismatchHandler {
        private final Map<String, Map<Long, List<Long>>> supportedSerialVersionUidsPerClass;

        private VersionMismatchHandler() {
            this.supportedSerialVersionUidsPerClass = new HashMap();
        }

        void addVersionsMatch(String className, long localVersionUID, List<Long> streamVersionUIDs) {
            ((Map) this.supportedSerialVersionUidsPerClass.computeIfAbsent(className, (k) -> {
                return new HashMap();
            })).put(localVersionUID, streamVersionUIDs);
        }

        boolean shouldTolerateSerialVersionMismatch(String className, long localVersionUID, long streamVersionUID) {
            return ((List) ((Map) this.supportedSerialVersionUidsPerClass.getOrDefault(className, Collections.emptyMap())).getOrDefault(localVersionUID, Collections.emptyList())).contains(streamVersionUID);
        }

        boolean haveRulesForClass(String className) {
            return this.supportedSerialVersionUidsPerClass.containsKey(className);
        }
    }

    public static class ClassLoaderObjectInputStream extends ObjectInputStream {
        protected final ClassLoader classLoader;
        private static final HashMap<String, Class<?>> primitiveClasses = CollectionUtil.newHashMapWithExpectedSize(9);
        public static ClassLoader classLoader2;

        public ClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
            super(in);
            this.classLoader = classLoader;
            if (classLoader2 == null) {
                classLoader2 = classLoader;
            }
        }

        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            if (this.classLoader2 != null) {
                String name = desc.getName();

                try {
                    return Class.forName(name, false, this.classLoader2);
                } catch (ClassNotFoundException var5) {
                    ClassNotFoundException ex = var5;
                    Class<?> cl = (Class) primitiveClasses.get(name);
                    if (cl != null) {
                        return cl;
                    } else {
                        throw ex;
                    }
                }
            } else {
                return super.resolveClass(desc);
            }
        }

        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            if (this.classLoader != null) {
                ClassLoader nonPublicLoader = null;
                boolean hasNonPublicInterface = false;
                Class<?>[] classObjs = new Class[interfaces.length];

                for (int i = 0; i < interfaces.length; ++i) {
                    Class<?> cl = Class.forName(interfaces[i], false, this.classLoader);
                    if ((cl.getModifiers() & 1) == 0) {
                        if (hasNonPublicInterface) {
                            if (nonPublicLoader != cl.getClassLoader()) {
                                throw new IllegalAccessError("conflicting non-public interface class loaders");
                            }
                        } else {
                            nonPublicLoader = cl.getClassLoader();
                            hasNonPublicInterface = true;
                        }
                    }

                    classObjs[i] = cl;
                }

                try {
                    return Proxy.getProxyClass(hasNonPublicInterface ? nonPublicLoader : this.classLoader, classObjs);
                } catch (IllegalArgumentException var7) {
                    throw new ClassNotFoundException((String) null, var7);
                }
            } else {
                return super.resolveProxyClass(interfaces);
            }
        }

        static {
            primitiveClasses.put("boolean", Boolean.TYPE);
            primitiveClasses.put("byte", Byte.TYPE);
            primitiveClasses.put("char", Character.TYPE);
            primitiveClasses.put("short", Short.TYPE);
            primitiveClasses.put("int", Integer.TYPE);
            primitiveClasses.put("long", Long.TYPE);
            primitiveClasses.put("float", Float.TYPE);
            primitiveClasses.put("double", Double.TYPE);
            primitiveClasses.put("void", Void.TYPE);
        }
    }
}
