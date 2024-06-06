package com.shark.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.shark.service.RpcServiceLocator;
import com.shark.gateway.MqttServer;

import java.io.IOException;

public class GateWayServer {

    private void start(int port) {
        try (RpcServiceLocator rpcServiceLocator = new RpcServiceLocator()) {
            rpcServiceLocator.init();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    rpcServiceLocator.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (IOException | NacosException e) {
            throw new RuntimeException(e);
        }

        try (MqttServer mqttServer = new MqttServer(port)) {
            mqttServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    mqttServer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException();
        }

        int port = Integer.parseInt(args[0]);

        GateWayServer gateWayServer = new GateWayServer();
        gateWayServer.start(port);
    }
}
