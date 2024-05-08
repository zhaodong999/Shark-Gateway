package org.shark;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;

public class GatewayHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage message = (MqttMessage) msg;
        System.out.println("Received MQTT message: " + message);

        switch (message.fixedHeader().messageType()) {
            case CONNECT:
                MqttConnectMessage connectMessage = (MqttConnectMessage) msg;
                handleConnect(ctx, connectMessage);
                break;
            case PUBLISH:
                MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                MqttFixedHeader mqttFixedHeader = publishMessage.fixedHeader();
                publishMessage.payload();

                break;
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage connectMessage) {
        MqttConnectVariableHeader mqttConnectVariableHeader = connectMessage.variableHeader();
        MqttConnectPayload payload = connectMessage.payload();
    }

    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage publishMessage) {
        MqttFixedHeader mqttFixedHeader = publishMessage.fixedHeader();
        publishMessage.payload();
    }
}
