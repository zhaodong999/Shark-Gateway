package com.shark.rpc.client;

import com.shark.rpc.*;
import com.shark.rpc.protomessage.Rpc;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.shark.IdUtils;

import java.util.concurrent.CompletableFuture;

public class RpcConnection {

    private final Bootstrap bootstrap;

    private final EndPoint endPoint;

    private Channel channel;

    private long period = 2;

    public RpcConnection(EndPoint endPoint, NioEventLoopGroup nioEventLoopGroup) {
        this.endPoint = endPoint;

        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .group(nioEventLoopGroup)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder());
                        ch.pipeline().addLast(new RpcDecoder());
                        ch.pipeline().addLast(new IdleStateHandler(60,30,0));
                        ch.pipeline().addLast(new RpcClientHandler());
                    }
                });

    }

    public void connect() {
        ChannelFuture channelFuture = bootstrap.connect(endPoint.getHost(), endPoint.getPort());
        channelFuture.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                channel = f.channel();
            } else {
                channel = null;
                //TODO 2,4,8,16,32,大于一分钟要重新从2开始
                period = period > 60 ? 2 : (long) Math.pow(period, 2);
                Thread.sleep(period * 1000);
                connect();
            }
        });
    }

    public void send(Rpc.RpcRequest rpcRequest, CompletableFuture<Rpc.RpcResponse> callBack) throws Exception {
        if (channel == null || !channel.isWritable()) {
            throw new Exception();
        }

        RpcMsg rpcMsg = new RpcMsg(ProtoCommand.Request);
        long id = IdUtils.getUniqueIdBySnakeflow();
        VariableHeader variableHeader = new VariableHeader(id, SerializeType.Proto);
        rpcMsg.setVariableHeader(variableHeader);
        rpcMsg.setPayLoad(rpcRequest.toByteArray());

        channel.writeAndFlush(rpcMsg).addListener(f -> {
            if (f.isSuccess()) {
                RequestHolder.put(id, callBack);
            } else {
                callBack.completeExceptionally(f.cause());
            }
        });
    }


    public boolean available() {
        return channel != null;
    }
}
