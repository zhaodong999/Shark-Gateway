package com.shark.gateway;

import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {

    private ConnectionManager() {
    }

    public static final ConcurrentMap<String, ChannelHandlerContext> CLIENT_CONNECTIONS = new ConcurrentHashMap<>();

    /**
     * TODO 终端，多终端，踢出上一个无效连接
     *
     * @param id
     * @param ctx
     */
    public static void putCtx(String id, ChannelHandlerContext ctx) {
        CLIENT_CONNECTIONS.put(id, ctx);
    }

    public static ChannelHandlerContext getCtx(String id) {
        return CLIENT_CONNECTIONS.get(id);
    }
}
