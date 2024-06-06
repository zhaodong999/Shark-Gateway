package com.shark.rpc.client;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.rpc.EndPoint;
import com.shark.rpc.protomessage.Rpc;

import java.util.concurrent.CompletableFuture;

/**
 * 只需要实现异步调用，不需要同步调用
 * 每个RpcClient实例跟主机绑定
 * 增加一个调用超时处理
 *
 * <li>
 * 暂时是单连接处理，以后变成连接池
 * </>
 */
public class RpcClient {

    private final EndPoint endPoint;

    private RpcConnection rpcConnection;

    public RpcClient(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public void connect(CallBack callBack) {
        rpcConnection = new RpcConnection(endPoint);
        rpcConnection.connect(callBack);
    }

    public CompletableFuture<Rpc.RpcResponse> call(Rpc.RpcRequest request) throws Exception {
        if (rpcConnection == null || !rpcConnection.available()) {
            //TODO Rpc 连接不可用
            throw new Exception();
        }

        CompletableFuture<Rpc.RpcResponse> callBack = new CompletableFuture<>();
        rpcConnection.send(request, callBack);
        return callBack;
    }

    public static void main(String[] args) throws Exception {
        //建立连接
        EndPoint endPoint = new EndPoint("localhost", 8880);
        //构建调用
        RpcClient rpcClient = new RpcClient(endPoint);
        rpcClient.connect(() -> {

        });
        Any params = Any.pack(StringValue.of("rpcClient"));

        //service login,  method say,  param rpcClient
        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("login").setMethod("say").addArgs(params).build();
        CompletableFuture<Rpc.RpcResponse> call = rpcClient.call(rpcRequest);

        //同步获得结果
        Rpc.RpcResponse rpcResponse = call.get();
        Any result = rpcResponse.getResult();
        StringValue unpack = result.unpack(StringValue.class);
        System.out.println(unpack.getValue());
    }

}
