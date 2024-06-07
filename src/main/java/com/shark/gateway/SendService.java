package com.shark.gateway;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.shark.rpc.server.annotation.RpcMethod;
import com.shark.rpc.server.annotation.RpcService;
import com.shark.server.msg.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.shark.gateway.AttrKey.SERVER_MSG_ID;

@RpcService(name = "gate")
public class SendService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendService.class);

    @RpcMethod(name = "publish")
    public void sendMsg(Server.ServerMessage serverMessage) {
        LOGGER.info("receive: {}\t{}", serverMessage.getId(), serverMessage.getTopic());
        byte[] body = serverMessage.toByteArray();

        //get connection channel
        ChannelHandlerContext ctx = ConnectionManager.getCtx(serverMessage.getId());
        int serverMsgId = ctx.channel().attr(SERVER_MSG_ID).get().getAndIncrement();

        //create publishMessage
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(serverMessage.getQos()), true, body.length);
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(serverMessage.getTopic(), serverMsgId);
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        MqttMessage mqttMessage = MqttMessageFactory.newMessage(fixedHeader, variableHeader, buffer);

        //send to client
        ctx.writeAndFlush(mqttMessage);
    }

    public void test(Any... anys) throws InvalidProtocolBufferException {
        Server.ServerMessage unpack = anys[0].unpack(Server.ServerMessage.class);


    }
}
