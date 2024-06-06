package com.shark.server;

import com.shark.rpc.server.RpcProxyManager;
import com.shark.rpc.server.RpcServer;
import com.shark.service.RpcServiceLocator;
import com.shark.service.ServiceOne;
import javassist.CannotCompileException;

import java.lang.reflect.InvocationTargetException;

public class GameServer {

    public void start(int port) {
        //注册服务
        RpcProxyManager rpcServiceManager = new RpcProxyManager();
        try {
            rpcServiceManager.register(new ServiceOne());
        } catch (CannotCompileException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        RpcServiceLocator rpcServiceLocator = new RpcServiceLocator();
        //启动rpc 监听端口，
        RpcServer rpcServer = new RpcServer(port, rpcServiceManager, rpcServiceLocator);
        try {
            rpcServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException();
        }

        int port = Integer.parseInt(args[0]);

        GameServer gameServer = new GameServer();
        gameServer.start(port);
    }
}
