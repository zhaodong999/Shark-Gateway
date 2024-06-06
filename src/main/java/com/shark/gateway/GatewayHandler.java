package com.shark.gateway;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class GatewayHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayHandler.class);

    public static final GatewayHandler INSTANCE = new GatewayHandler();

    private GatewayHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage message = (MqttMessage) msg;
        LOGGER.info("Received MQTT message: {}", message);

        switch (message.fixedHeader().messageType()) {
            case CONNECT:
                MqttProcessor.CONNECT.handle(ctx, message);
                break;
            case PUBLISH:
                MqttProcessor.PUBLISH.handle(ctx, message);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        LOGGER.error("handle error", cause);
    }
}
