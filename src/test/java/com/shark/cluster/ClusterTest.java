package com.shark.cluster;

import com.alibaba.nacos.api.exception.NacosException;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.rpc.client.RpcClient;
import com.shark.rpc.protomessage.Rpc;
import com.shark.server.msg.Server;
import com.shark.service.BalanceType;
import com.shark.service.RpcClusterFactory;
import com.shark.service.RpcServiceLocator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

public class ClusterTest {

    @Test
    public void test() throws Exception {
        RpcServiceLocator rpcServiceLocator = new RpcServiceLocator();
        rpcServiceLocator.connectCluster();
        rpcServiceLocator.loadServices();

        RpcClusterFactory.init(rpcServiceLocator);

        RpcClient rpcClient = RpcClusterFactory.getRpcClient("gate", BalanceType.Hash, "userId_001");

        Server.ServerMessage serverMessage = Server.ServerMessage.newBuilder().setId("userId_001").setTopic("/sys/game").setQos(0).setCmd(1).setBody(Any.pack(StringValue.of("ffff"))).build();
        Any any = Any.pack(serverMessage);
        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("gate").setMethod("publish").addArgs(any).build();
        CompletableFuture<Rpc.RpcResponse> completableFuture = rpcClient.call(rpcRequest);

        completableFuture.whenComplete((response, throwable) -> {
            if(throwable == null){
                System.out.println("success");
            }
        });

        Thread.currentThread().join();
    }
}
