package com.shark.rpc.proxy;

import com.google.protobuf.GeneratedMessageV3;
import com.shark.gateway.SendService;
import com.shark.rpc.server.RpcProxyManager;
import com.shark.server.msg.Server;
import javassist.CannotCompileException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ProxyTest {

    @Test
    public void test() throws CannotCompileException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        SendService sendService = new SendService();
        RpcProxyManager rpcProxyManager = new RpcProxyManager();
        rpcProxyManager.register(sendService);
    }

    @Test
    public void testExtends() {
        System.out.println("super: " + Server.ServerMessage.class.getSuperclass());
        boolean result = GeneratedMessageV3.class.isAssignableFrom(Server.ServerMessage.class);
        System.out.println("is extend: " + result);
    }
}
