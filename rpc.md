## rpc协议

* fixedHeader和body

| 字节       | 描述      | 描述                         |
| ---------- | --------- | ---------------------------- |
| 2B         | Magic     | 魔数字                       |
| 4b         | version   | 版本                         |
| 4b         | command   | request, response,ping, pong |
| 1B         | serialize | 1.proto                      |
| 2B         | Length    | payload长度                  |
| 8B         | requestId |                              |
| 。。。。。 | payload   | 序列化内容                   |





RpcRequest

| 属性    | 描述     |
| ------- | -------- |
| service | 服务     |
| method  | 调用方法 |
| Args    | 参数列表 |

RpcResponse

| 属性   | 描述                 |
| ------ | -------------------- |
| status | 0成功， 其余全是失败 |
| Result | 返回值               |
| reason | 失败原因             |

## 调用接口

* Service

~~~java
package com.shark.service;

import com.google.protobuf.*;
import com.shark.rpc.server.annotation.RpcMethod;
import com.shark.rpc.server.annotation.RpcService;

@RpcService(name = "login")
public class ServiceOne {
  
    @RpcMethod(name = "say")
    public String say(String name) {
        return "hello: " + name;
    }
}
~~~

* Server

~~~java
    public static void main(String[] args) {
        //注册服务
        RpcServiceManager rpcServiceManager = new RpcServiceManager();
        try {
            rpcServiceManager.register(new ServiceOne());
        } catch (CannotCompileException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        //启动rpc 监听端口，
        RpcServer rpcServer = new RpcServer(8880, rpcServiceManager);
        try {
            rpcServer.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
~~~

* Client

~~~java
  public static void main(String[] args) throws Exception {
        //建立连接
        EndPoint endPoint = new EndPoint("localhost", 8880);
        ConnManager connManager = new ConnManager();
        connManager.registerEndPoint(endPoint);

        //构建调用
        RpcClient rpcClient = new RpcClient(connManager, endPoint);
        Any params = Any.pack(StringValue.of("rpcClient"));

        //service login,  method say,  param rpcClient
        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("login").setMethod("say").addArgs(params).build();
        CompletableFuture<Rpc.RpcResponse> call = rpcClient.call(rpcRequest);

        //同步获得结果
        Rpc.RpcResponse rpcResponse = call.get();
        Any result = rpcResponse.getResult();
        StringValue unpack = result.unpack(StringValue.class);
        System.out.println(unpack.getValue());
    }
~~~

