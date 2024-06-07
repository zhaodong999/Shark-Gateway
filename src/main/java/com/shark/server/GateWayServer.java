package com.shark.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.shark.gateway.SendService;
import com.shark.service.RpcServiceLocator;
import com.shark.gateway.MqttServer;
import com.shark.util.IpUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GateWayServer {

    private void start(int port) {
        String ip = IpUtils.getIp();
        try (RpcServiceLocator rpcServiceLocator = new RpcServiceLocator()) {
            rpcServiceLocator.connectCluster();
            Set<String> serviceNames = new HashSet<>();
//            rpcServiceLocator.registerInstance(serviceNames);
            rpcServiceLocator.loadServices();

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
