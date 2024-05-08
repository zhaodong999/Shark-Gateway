package com.shark.rpc.server;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.shark.rpc.server.annotation.RpcMethod;
import com.shark.rpc.server.annotation.RpcService;
import com.shark.service.ProxyWrapper;
import com.shark.service.ServiceOne;
import com.shark.util.ServiceUtil;
import javassist.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class RpcServiceManager {

    private static final String ROOT_PATH = "com.shark.service";

    private static final String SUPER_CLASS_NAME = "ProxyWrapper";

    private static final String PROTO_BUF_ANY_NAME = "com.google.protobuf.Any";

    private final ConcurrentHashMap<ServiceId, ProxyWrapper> proxyMethods = new ConcurrentHashMap<>();

    public CompletableFuture<Any> invoke(String serviceName, String method, Any... params) {
        return CompletableFuture.supplyAsync(() -> {
            ServiceId serviceId = new ServiceId(serviceName, method);
            ProxyWrapper proxyWrapper = proxyMethods.get(serviceId);
            return proxyWrapper.executor(params);
        });
    }


    public void register(Object serviceInstance) throws CannotCompileException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> serviceClass = serviceInstance.getClass();
        Method[] methods = serviceClass.getMethods();
        RpcService rpcServiceAnno = serviceClass.getAnnotation(RpcService.class);

        for (Method method : methods) {
            RpcMethod rpcMethodAnno = method.getAnnotation(RpcMethod.class);
            if (rpcMethodAnno == null) {
                continue;
            }

            ServiceId serviceId = new ServiceId(rpcServiceAnno.name(), rpcMethodAnno.name());
            CtClass ctClass;
            try {
                ctClass = generateClass(serviceId, serviceClass, method);
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }

            try {
                ctClass.writeFile("/Users/zhaodong");
            } catch (CannotCompileException | IOException e) {
                throw new RuntimeException(e);
            }


            Class<?> proxyClass = ctClass.toClass();
            Constructor<?> declaredConstructor = proxyClass.getDeclaredConstructor(serviceClass);
            ProxyWrapper proxyInstance = (ProxyWrapper) declaredConstructor.newInstance(serviceInstance);
            proxyMethods.put(serviceId, proxyInstance);
        }
    }

    private String generateBody(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n{\n");

        //generate param
        int index = 0;
        for (Type genericParameterType : method.getGenericParameterTypes()) {
            if (genericParameterType.getClass().isPrimitive() || genericParameterType == String.class) {
                unbox(sb, genericParameterType, index);
            } else {

            }
            ++index;
        }

        //invoke method
        index = 0;
        sb.append(method.getReturnType().getName()).append(" result = serviceInstance.").append(method.getName()).append("(");
        for (Type genericParameterType : method.getGenericParameterTypes()) {
            sb.append("param").append("_").append(index);
            ++index;
        }
        sb.append(");\n");

        //result
        if (method.getReturnType().isPrimitive() || method.getReturnType() == String.class) {
            boxResult(sb, method.getReturnType());
        }
        sb.append("return com.google.protobuf.Any.pack(value);\n");
        sb.append("}\n");

        System.out.println(sb.toString());
        return sb.toString();
    }

    private CtClass generateClass(ServiceId serviceId, Class<?> serviceClass, Method method) throws NotFoundException, CannotCompileException {
        ClassPool classPool = ClassPool.getDefault();
        CtClass rootClass = classPool.get(ROOT_PATH + "." + SUPER_CLASS_NAME);
        CtClass subClass = classPool.makeClass(serviceClass.getName() + ServiceUtil.firstUpperCase(serviceId.getMethodName()) + "Wrapper");
        subClass.setSuperclass(rootClass);

        CtField ctField = new CtField(classPool.get(serviceClass.getName()), "serviceInstance", subClass);
        ctField.setModifiers(Modifier.PRIVATE);
        subClass.addField(ctField);

        CtConstructor ctConstructor = new CtConstructor(new CtClass[]{classPool.get(serviceClass.getName())}, subClass);
        ctConstructor.setBody("{$0.serviceInstance = $1;}");
        subClass.addConstructor(ctConstructor);

        CtMethod ctMethod = new CtMethod(classPool.get(PROTO_BUF_ANY_NAME), "executor", new CtClass[]{classPool.get("com.google.protobuf.Any[]")}, subClass);
        ctMethod.setModifiers(Modifier.PUBLIC | Modifier.VARARGS);
        subClass.addMethod(ctMethod);
        ctMethod.setBody(generateBody(method));
//        ctMethod.setBody("return null;");
        return subClass;
    }

    private void boxResult(StringBuilder sb, Class<?> returnType) {
        if (returnType == int.class) {
            box(sb, "com.google.protobuf.Int32Value");
        } else if (returnType == String.class) {
            box(sb, "com.google.protobuf.StringValue");
        } else if (returnType == long.class) {
            box(sb, "com.google.protobuf.LongValue");
        } else if (returnType == double.class) {
            box(sb, "com.google.protobuf.DoubleValue");
        }
    }

    public void box(StringBuilder sb, String pbClassName) {
        sb.append(pbClassName).append(" value = ");
        sb.append(pbClassName).append(".newBuilder().setValue(result).build();\n");
    }

    private void unbox(StringBuilder sb, Type genericParameterType, int index) {
        if (genericParameterType == int.class) {
            unbox(sb, index, "com.google.protobuf.Int32Value", "int");
        } else if (genericParameterType == String.class) {
            unbox(sb, index, "com.google.protobuf.StringValue", "String");
        } else if (genericParameterType == long.class) {
            unbox(sb, index, "com.google.protobuf.Int64Value", "long");
        } else if (genericParameterType == double.class) {
            unbox(sb, index, "com.google.protobuf.DoubleValue", "double");
        }
    }

    private void unbox(StringBuilder sb, int index, String pbClassName, String primitiveName) {
        String pbVar = "unpack_" + index;
        String param = "param_" + index;
        sb.append(pbClassName).append(" ").append(pbVar).append(" = ").append("(").append(pbClassName).append(")").append("$1[").append(index).append("].unpack(").append(pbClassName).append(".class);\n")
                .append(primitiveName).append(" ").append(param).append(" = ").append(pbVar).append(".getValue();\n");
    }

    public static class ServiceId {
        private String serviceName;
        private String methodName;

        public ServiceId(String serviceName, String methodName) {
            this.serviceName = serviceName;
            this.methodName = methodName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ServiceId serviceId = (ServiceId) o;
            return Objects.equals(serviceName, serviceId.serviceName) && Objects.equals(methodName, serviceId.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, methodName);
        }
    }

    public static void main(String[] args) throws CannotCompileException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ExecutionException, InterruptedException {
        RpcServiceManager rpcServiceManager = new RpcServiceManager();
        rpcServiceManager.register(new ServiceOne());

        StringValue build = StringValue.newBuilder().setValue("ttt").build();
        CompletableFuture<Any> result = rpcServiceManager.invoke("login", "say", Any.pack(build));

        Any any = result.get();
        try {
            StringValue unpack = any.unpack(StringValue.class);
            System.out.println(unpack.getValue());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }

    }
}
