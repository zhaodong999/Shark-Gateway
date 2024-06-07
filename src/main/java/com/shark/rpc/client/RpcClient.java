package com.shark.rpc.client;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.rpc.EndPoint;
import com.shark.rpc.protomessage.Rpc;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 只需要实现异步调用，不需要同步调用
 * 每个RpcClient实例跟主机绑定
 * 增加一个调用超时处理
 * 延迟建立连接，未必所有服务都要调用
 * <li>
 * 暂时是单连接处理，以后变成连接池
 * </>
 */
public class RpcClient {

    private final EndPoint endPoint;

    private RpcConnection rpcConnection;

    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicBoolean isInit = new AtomicBoolean(false);

    private NioEventLoopGroup nioEventLoopGroup;

    public RpcClient(EndPoint endPoint, NioEventLoopGroup nioEventLoopGroup) {
        this.endPoint = endPoint;
        this.nioEventLoopGroup = nioEventLoopGroup;
    }

    public void connect() {
        rpcConnection = new RpcConnection(endPoint, nioEventLoopGroup);
        rpcConnection.connect();
    }

    public CompletableFuture<Rpc.RpcResponse> call(Rpc.RpcRequest request) throws Exception {
        if (!rpcConnection.available()) {
            throw new Exception();
        }
        CompletableFuture<Rpc.RpcResponse> callBack = new CompletableFuture<>();
        rpcConnection.send(request, callBack);
        return callBack;
    }

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        //建立连接
        EndPoint endPoint = new EndPoint("localhost", 8880);
        //构建调用
        RpcClient rpcClient = new RpcClient(endPoint, nioEventLoopGroup);
        rpcClient.connect();
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
