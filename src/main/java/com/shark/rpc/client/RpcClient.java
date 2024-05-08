package com.shark.rpc.client;

import com.shark.rpc.EndPoint;
import com.shark.rpc.protomessage.Rpc;

import java.util.concurrent.CompletableFuture;

/**
 * 只需要实现异步调用，不需要同步调用
 * 每个RpcClient实例跟主机绑定
 * 增加一个调用超时处理
 *
 * <li>
 *     暂时是单连接处理，以后变成连接池
 * </>
 *
 */
public class RpcClient {

    private final EndPoint endPoint;

    private final ConnManager connManager;

    public RpcClient(ConnManager connManager, EndPoint endPoint) {
        this.connManager = connManager;
        this.endPoint = endPoint;
    }

    public CompletableFuture<Rpc.RpcResponse> call(Rpc.RpcRequest request) throws Exception {
        RpcConnection rpcConnection = connManager.getRpcConnection(endPoint);
        if (rpcConnection == null || !rpcConnection.available()) {
            //TODO Rpc 连接不可用
            throw new Exception();
        }

        CompletableFuture<Rpc.RpcResponse> callBack = new CompletableFuture<>();
        rpcConnection.send(request, callBack);
        return callBack;
    }

    public static void main(String[] args) throws Exception {
        EndPoint endPoint = new EndPoint("localhost", 8880);
        ConnManager connManager = new ConnManager();
        connManager.registerEndPoint(endPoint);

        Thread.sleep(70 * 1000);
//        RpcClient rpcClient = new RpcClient(connManager, endPoint);
//
//        Any params = Any.pack(StringValue.of("rpcClient"));
//        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("login").setMethod("say").addArgs(params).build();
//        CompletableFuture<Rpc.RpcResponse> call = rpcClient.call(rpcRequest);
//        Rpc.RpcResponse rpcResponse = call.get();
//        Any result = rpcResponse.getResult();
//        StringValue unpack = result.unpack(StringValue.class);
//        System.out.println(unpack.getValue());
    }

}
