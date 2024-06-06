package com.shark.service;

import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.shark.rpc.EndPoint;
import com.shark.rpc.client.RpcClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public class RpcService implements Observer {

    private String name;

    private RpcServiceLocator rpcServiceLocator;

    private final CopyOnWriteArrayList<RpcClient> rpcClients = new CopyOnWriteArrayList<>();

    public RpcService(String name, RpcServiceLocator rpcServiceLocator) {
        this.name = name;
        this.rpcServiceLocator = rpcServiceLocator;

        List<Instance> rpcInstances = this.rpcServiceLocator.getInstance(name);
        if (rpcInstances != null && !rpcInstances.isEmpty()) {
            for (Instance instance : rpcInstances) {
                initRpcClient(instance);
            }
        }

        rpcServiceLocator.addObserver(this);
    }

    private void initRpcClient(Instance instance) {
        EndPoint endPoint = new EndPoint(instance.getIp(), instance.getPort());
        RpcClient rpcClient = new RpcClient(endPoint);
        rpcClient.connect(() -> {
            rpcClients.add(rpcClient);
        });
    }

    public RpcClient getRpcClient(BalanceType hash, String id) {
        HashFunction hashFunction = Hashing.murmur3_128();
        HashCode hashCode = hashFunction.hashBytes(id.getBytes(StandardCharsets.UTF_8));
        int bucket = Hashing.consistentHash(hashCode, rpcClients.size());
        return rpcClients.get(bucket);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof RpcServiceLocator)) {
            return;
        }

        NamingEvent namingEvent = (NamingEvent) arg;
        if (!namingEvent.getServiceName().equals(name)) {
            return;
        }

        //TODO
        List<Instance> currInstances = namingEvent.getInstances();
        List<Instance> lastInstances = rpcServiceLocator.getInstance(name);
    }
}
