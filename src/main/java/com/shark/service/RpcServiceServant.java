package com.shark.service;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RpcServiceServant {

    private static final ConcurrentMap<String, RpcService> services = new ConcurrentHashMap<>();

    private static final RpcServiceLocator rpcServiceLocator = new RpcServiceLocator();

    static{
        try {
            rpcServiceLocator.init();
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    public static RpcService getService(String serviceName) {
        if (services.containsKey(serviceName)) {
            return services.get(serviceName);
        }

        RpcService rpcService = new RpcService(serviceName, rpcServiceLocator);
        services.put(serviceName, rpcService);
        return rpcService;
    }
}
