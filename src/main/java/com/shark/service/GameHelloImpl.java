package com.shark.service;

public class GameHelloImpl implements HelloService {
    @Override
    public String sayHello(String param) {
        System.out.println("hello world");
        return "hello" + param;
    }
}
