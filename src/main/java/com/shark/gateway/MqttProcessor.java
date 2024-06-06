package com.shark.gateway;

import com.google.protobuf.InvalidProtocolBufferException;
import com.shark.rpc.protomessage.Rpc;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public enum MqttProcessor {

    CONNECT {
        @Override
        public void handle(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
            MqttConnectMessage connectMessage = (MqttConnectMessage) mqttMessage;
            LOGGER.info("connectMsg: {}", connectMessage);
            MqttConnectPayload payload = connectMessage.payload();
            String id = payload.clientIdentifier();
            String name = payload.userName();
            String password = new String(payload.passwordInBytes());

            CompletableFuture<AuthResult> future = AuthService.auth(id, password);
            future.whenComplete((authResult, throwable) -> {
                MqttFixedHeader connAckFixedHeader =
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
                MqttConnAckVariableHeader connAckVariableHeader;
                if (throwable == null) {
                    connAckVariableHeader =
                            new MqttConnAckVariableHeader(authResult.getCode(), false);
                } else {
                    connAckVariableHeader =
                            new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE, false);
                }

                ConnectionManager.putCtx(id, ctx);
                MqttMessage connAckMsg = MqttMessageFactory.newMessage(connAckFixedHeader, connAckVariableHeader, null);
                ctx.writeAndFlush(connAckMsg);
            });
        }
    },

    PUBLISH {
        @Override
        public void handle(ChannelHandlerContext ctx, MqttMessage mqttMessage) {
            MqttPublishMessage publishMsg = (MqttPublishMessage) mqttMessage;
            ByteBuf payload = publishMsg.payload();
            byte[] body = new byte[payload.capacity()];
            payload.readBytes(body);

            Rpc.RpcRequest request;
            try {
                request = Rpc.RpcRequest.parseFrom(body);
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }

            if (request == null) {
                return;
            }

            switch (publishMsg.fixedHeader().qosLevel()) {
                case AT_MOST_ONCE:
                    //最多一次，不需要回复
                    PublishService.handleOne(request);
                    break;
                case AT_LEAST_ONCE:
                    //回复一个publishAck,会传递多次
                    CompletableFuture<Rpc.RpcResponse> ackFuture = PublishService.handle(request);
                    ackFuture.whenComplete((response, throwable) -> {
                        MqttFixedHeader fixedHeader =
                                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);

                        MqttMessage pubAckMsg = MqttMessageFactory.newMessage(fixedHeader, MqttMessageIdVariableHeader.from(publishMsg.variableHeader().packetId()), null);
                        ctx.writeAndFlush(pubAckMsg);
                    });
                    break;
                case EXACTLY_ONCE:
                    CompletableFuture<Rpc.RpcResponse> recFuture = PublishService.handle(request);
                    recFuture.whenComplete((response, throwable) -> {
                        MqttFixedHeader fixedHeader =
                                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
                        MqttMessage pubRecMsg = MqttMessageFactory.newMessage(fixedHeader, MqttMessageIdVariableHeader.from(publishMsg.variableHeader().packetId()), null);
                        ctx.writeAndFlush(pubRecMsg);
                    });
                    break;
            }
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttProcessor.class);

    public abstract void handle(ChannelHandlerContext ctx, MqttMessage mqttMessage);
}
