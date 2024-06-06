package com.shark.rpc.server;

import com.google.protobuf.Any;
import com.shark.rpc.ProtoCommand;
import com.shark.rpc.RpcMsg;
import com.shark.rpc.SerializeType;
import com.shark.rpc.VariableHeader;
import com.shark.rpc.protomessage.Rpc;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcMsg> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);

    private final RpcProxyManager rpcServiceManager;

    public RpcServerHandler(RpcProxyManager rpcServiceManager) {
        this.rpcServiceManager = rpcServiceManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMsg msg) throws Exception {
        if (msg.getFixedHeader().getProtoCommand() == ProtoCommand.Ping) {
            ctx.writeAndFlush(RpcMsg.PONG);
            return;
        }

        if (msg.getFixedHeader().getProtoCommand() == ProtoCommand.Request) {
            byte[] payLoad = msg.getPayLoad();
            Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.parseFrom(payLoad);
            Any[] params = new Any[rpcRequest.getArgsCount()];
            rpcRequest.getArgsList().toArray(params);

            LOGGER.info("receive packet: {}/{}", rpcRequest.getService(), rpcRequest.getMethod());
            CompletableFuture<Any> completableFuture = rpcServiceManager.invoke(rpcRequest.getService(), rpcRequest.getMethod(), params);

            completableFuture.thenAccept(any -> {
                Rpc.RpcResponse.Builder builder = Rpc.RpcResponse.newBuilder();
                builder.setStatus(Rpc.RpcStatus.OK);
                builder.setResult(any);
                builder.build();

                RpcMsg rpcMsg = new RpcMsg(ProtoCommand.Response);
                VariableHeader variableHeader = new VariableHeader(msg.getVariableHeader().getTrackerId(), SerializeType.Proto);
                rpcMsg.setVariableHeader(variableHeader);
                rpcMsg.setPayLoad(builder.build().toByteArray());

                ctx.channel().writeAndFlush(rpcMsg);
            });
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                LOGGER.warn("rpc server close conn when read idle");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("rpc server error", cause);
        ctx.close();
    }
}
