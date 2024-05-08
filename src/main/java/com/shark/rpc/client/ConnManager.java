package com.shark.rpc.client;

import com.shark.rpc.EndPoint;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 主要考虑到有可能断线重连，需要一个netty连接的外壳管理
 */
public class ConnManager {

    public final ConcurrentHashMap<EndPoint, RpcConnection> conns = new ConcurrentHashMap<>();

    public void registerEndPoint(EndPoint endPoint) {
        RpcConnection rpcConnection = new RpcConnection(endPoint);
        rpcConnection.connect();
        conns.put(endPoint, rpcConnection);
    }

    public RpcConnection getRpcConnection(EndPoint endPoint){
        return conns.get(endPoint);
    }

}
