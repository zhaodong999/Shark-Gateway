package com.shark.service;

import com.google.protobuf.*;
import com.shark.rpc.server.annotation.RpcMethod;
import com.shark.rpc.server.annotation.RpcService;

@RpcService(name = "login")
public class ServiceOne {

    private GameHelloImpl gameHello;
    @RpcMethod(name = "say")
    public String say(String name) {
        return "hello: " + name;
    }

    public Any test(Any[] anys) throws InvalidProtocolBufferException {
        com.google.protobuf.StringValue unpack_0 = (StringValue)anys[0].unpack(com.google.protobuf.StringValue.class);
        String param_0 = unpack_0.getValue();
        java.lang.String result = gameHello.sayHello(param_0);
        com.google.protobuf.StringValue value = com.google.protobuf.StringValue.newBuilder().setValue(result).build();
        return com.google.protobuf.Any.pack(value);
    }
}
