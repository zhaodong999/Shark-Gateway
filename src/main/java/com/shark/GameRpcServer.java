package com.shark;

import com.shark.rpc.server.RpcProxyManager;
import com.shark.service.ServiceOne;
import javassist.CannotCompileException;

import java.lang.reflect.InvocationTargetException;

public class GameRpcServer {

    public static void main(String[] args) throws CannotCompileException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RpcProxyManager rpcServiceManager = new RpcProxyManager();
        rpcServiceManager.register(ServiceOne.class);
    }
}
