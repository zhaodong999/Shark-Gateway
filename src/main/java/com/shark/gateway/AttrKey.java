package com.shark.gateway;

import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicInteger;

public class AttrKey {

    /*服务端自增id*/
    public static final AttributeKey<AtomicInteger> SERVER_MSG_ID = AttributeKey.valueOf("serverMsgId");

}
