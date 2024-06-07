package com.shark.cluster;

import com.alibaba.nacos.api.exception.NacosException;
import com.shark.gateway.SendService;
import com.shark.rpc.server.RpcProxyManager;
import com.shark.rpc.server.RpcServer;
import com.shark.service.RpcServiceLocator;
import javassist.CannotCompileException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class ServerTest {

    @Test
    public void test() throws NacosException, InterruptedException, CannotCompileException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RpcServiceLocator rpcServiceLocator = new RpcServiceLocator();
        rpcServiceLocator.connectCluster();

        RpcProxyManager rpcProxyManager = new RpcProxyManager();
        rpcProxyManager.register(new SendService());

        RpcServer rpcServer = new RpcServer(8888, rpcProxyManager, rpcServiceLocator);
        rpcServer.start();
    }
}
