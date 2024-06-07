package com.shark.service;

import com.shark.rpc.EndPoint;
import com.shark.rpc.client.RpcClient;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 保证相同的主机下的服务RpcClient只有一个
 */
public class RpcClientFactory {


    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private static final ConcurrentMap<EndPoint, RpcClient> RPC_CLIENTS = new ConcurrentHashMap<>();

    public static synchronized RpcClient getRpcClient(EndPoint endPoint) {
        if (RPC_CLIENTS.containsKey(endPoint)) {
            return RPC_CLIENTS.get(endPoint);
        }

        RpcClient rpcClient = new RpcClient(endPoint, nioEventLoopGroup);
        rpcClient.connect();
        RPC_CLIENTS.put(endPoint, rpcClient);
        return rpcClient;
    }
}
