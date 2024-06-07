package com.shark.rpc.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.shark.rpc.RpcDecoder;
import com.shark.rpc.RpcEncoder;
import com.shark.service.RpcServiceLocator;
import com.shark.service.ServiceOne;
import com.shark.util.IpUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private final int port;

    private final RpcProxyManager rpcProxyManager;

    private final RpcServiceLocator rpcServiceLocator;

    public RpcServer(int port, RpcProxyManager rpcProxyManager, RpcServiceLocator rpcServiceLocator) {
        this.port = port;
        this.rpcProxyManager = rpcProxyManager;
        this.rpcServiceLocator = rpcServiceLocator;
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
                            ch.pipeline().addLast(new RpcServerHandler(rpcProxyManager));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            channelFuture.addListener(f -> {
                if (f.isSuccess()) {
                    publishService();
                }
            });
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void publishService() {
        try {
            rpcServiceLocator.loadServices();
        } catch (NacosException e) {
            LOGGER.error("connect cluster err", e);
        }

        rpcServiceLocator.registerInstance(rpcProxyManager.getServiceNames(), IpUtils.getIp(), port);
    }

    public static void main(String[] args) {
        //注册服务
        RpcProxyManager rpcServiceManager = new RpcProxyManager();
        try {
            rpcServiceManager.register(new ServiceOne());
        } catch (CannotCompileException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        String ip = IpUtils.getIp();
        RpcServiceLocator rpcServiceLocator = new RpcServiceLocator();
        //启动rpc 监听端口，
        RpcServer rpcServer = new RpcServer(8880, rpcServiceManager, rpcServiceLocator);
        try {
            rpcServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
