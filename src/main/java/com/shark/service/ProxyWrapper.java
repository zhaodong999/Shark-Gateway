package com.shark.service;

import com.google.protobuf.Any;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class ProxyWrapper {

    public abstract Any executor(Any... params);

    public static void main(String[] args) {
        Method[] methods = ProxyWrapper.class.getMethods();
        for (Method method: methods) {
            System.out.println(method.getName());
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            for (Type genericParameterType : genericParameterTypes) {
                System.out.println(genericParameterType.getTypeName());
            }
        }
    }

}
