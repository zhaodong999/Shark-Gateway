package com.shark.rpc.netty;

import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.rpc.protomessage.Rpc;
import org.junit.jupiter.api.Test;

public class RpcResponseTest {

    @Test
    public void test(){
        Rpc.RpcResponse response = Rpc.RpcResponse.newBuilder().setStatus(Rpc.RpcStatus.OK).build();
        response.toByteArray();

    }
}
