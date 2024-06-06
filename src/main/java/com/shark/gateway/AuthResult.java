package com.shark.gateway;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;

public class AuthResult {

    private MqttConnectReturnCode code;

    public MqttConnectReturnCode getCode() {
        return code;
    }
}
