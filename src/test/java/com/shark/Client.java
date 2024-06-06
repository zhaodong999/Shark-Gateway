package com.shark;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.rpc.protomessage.Rpc;
import com.shark.util.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class Client {

    private Channel channel;


    public Channel getChannel() {
        return channel;
    }

    public void start() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("encoder", MqttEncoder.INSTANCE);
                    ch.pipeline().addLast("decoder", new MqttDecoder());
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                    ch.pipeline().addLast(new ClientHandler());
//                    ch.pipeline().addLast("heartBeatHandler", new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS));
//                    ch.pipeline().addLast("handler", new MqttHeartBeatClientHandler(CLIENT_ID, USER_NAME, PASSWORD));
                }
            });

            ChannelFuture f = b.connect("192.168.31.122", 8887).sync();
            f.addListener((ChannelFutureListener) ff -> {
                if (f.isSuccess()) {
                    System.out.println("Client connected");
                    channel = f.channel();
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.start();

        Any params = Any.pack(StringValue.of("rpcClient"));
        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("login").setMethod("say").addArgs(params).build();

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(rpcRequest.toByteArray().length);
        buffer.writeBytes(rpcRequest.toByteArray());

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.AT_LEAST_ONCE, false, rpcRequest.toByteArray().length);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader("sys", 1);
        MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, variableHeader, buffer);

        Thread.sleep(1000 * 3);
        client.getChannel().writeAndFlush(mqttPublishMessage);

        Thread.currentThread().join();
    }
}
