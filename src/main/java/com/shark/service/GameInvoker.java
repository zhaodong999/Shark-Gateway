package com.shark.service;

import javassist.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class GameInvoker {

    public void run() throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("com.shark.service.ProxyWrapper");
        CtClass subClass = pool.makeClass("com.shark.service.SubWrapper");
        subClass.setSuperclass(ctClass);

        CtMethod newMethod = new CtMethod(CtClass.voidType, "executor", new CtClass[]{pool.get("java.lang.Object"), pool.get("java.lang.String")},subClass);
        newMethod.setModifiers(Modifier.PUBLIC);
        newMethod.setBody("{com.shark.service.HelloService service = (com.shark.service.HelloService)$1;\n" +
                "        if($2.equals(\"sayHello\")){\n" +
                "            service.sayHello();\n" +
                "        }}");
        subClass.addMethod(newMethod);
        Class<?> aClass = null;
        try {
            aClass = subClass.toClass();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            ProxyWrapper proxy = (ProxyWrapper) aClass.getDeclaredConstructor().newInstance();
//            proxy.executor(new GameHelloImpl(), "sayHello");
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        GameInvoker gameInvoker = new GameInvoker();
        try {
            gameInvoker.run();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            System.out.println("error !!!!! ");
        }
    }
}
