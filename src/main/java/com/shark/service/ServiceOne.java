package com.shark.service;

import com.shark.rpc.server.annotation.RpcMethod;
import com.shark.rpc.server.annotation.RpcService;

@RpcService(name = "login")
public class ServiceOne {

    @RpcMethod(name = "say")
    public String say(String name) {
        return "hello: " + name;
    }

}
