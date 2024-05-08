package com.shark.rpc.server;

import com.shark.rpc.RpcDecoder;
import com.shark.rpc.RpcEncoder;
import com.shark.service.ServiceOne;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import javassist.CannotCompileException;

import java.lang.reflect.InvocationTargetException;

public class RpcServer {

    private final int port;

    private final RpcServiceManager rpcServiceManager;

    public RpcServer(int port, RpcServiceManager rpcServiceManager) {
        this.port = port;
        this.rpcServiceManager = rpcServiceManager;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcEncoder());
                            ch.pipeline().addLast(new RpcDecoder());
                            ch.pipeline().addLast(new IdleStateHandler(45, 0, 0));
                            ch.pipeline().addLast(new RpcServerHandler(rpcServiceManager));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        //注册服务
        RpcServiceManager rpcServiceManager = new RpcServiceManager();
        try {
            rpcServiceManager.register(new ServiceOne());
        } catch (CannotCompileException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        //启动rpc 监听端口，
        RpcServer rpcServer = new RpcServer(8880, rpcServiceManager);
        try {
            rpcServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
