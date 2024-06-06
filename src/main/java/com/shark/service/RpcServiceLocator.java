package com.shark.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.google.protobuf.Any;
import com.google.protobuf.StringValue;
import com.shark.config.ConfigManager;
import com.shark.rpc.client.RpcClient;
import com.shark.rpc.protomessage.Rpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.*;


public class RpcServiceLocator extends Observable implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceLocator.class);
    private final ConcurrentMap<String, List<Instance>> serviceInstances = new ConcurrentHashMap<>();
    private NamingService namingService;
    private final static int PAGE_SIZE = 20;
    private ScheduledExecutorService service;

    @Override
    public void close() throws IOException {
        if (service != null) {
            service.shutdown();
        }
    }

    public void connectCluster() throws NacosException {
        namingService = NamingFactory.createNamingService(ConfigManager.getInstance().getServiceRegistrationAddr());
    }

    public void registerInstance(Set<String> serviceNames, String ip, int port) {
        for (String serviceName : serviceNames) {
            try {
                namingService.registerInstance(serviceName, ip, port);
            } catch (NacosException e) {
                LOGGER.error("register service err", e);
            }
        }
    }

    public void init() throws NacosException {
        connectCluster();
        pollLoadNewService();

        LOGGER.info("start schedule");
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                pollLoadNewService();
            } catch (Exception e) {
                LOGGER.error("poll instances err", e);
            }
        }, 20, 10, TimeUnit.SECONDS);

        LOGGER.info("stop schedule");
    }

    private void pollLoadNewService() {
        LOGGER.info("poll service");
        int page = 1;
        while (true) {
            try {
                LOGGER.info("poll service page");
                ListView<String> servicesOfServer = namingService.getServicesOfServer(page, PAGE_SIZE);
                if (servicesOfServer.getCount() == 0) {
                    break;
                }

                List<String> serviceNames = servicesOfServer.getData();
                for (String serviceName : serviceNames) {
                    //服务已经初始化注册过了
                    if (serviceInstances.containsKey(serviceName)) {
                        continue;
                    }

                    //注册服务， 注册服务的监听
                    List<Instance> allInstances = namingService.getAllInstances(serviceName);
                    serviceInstances.put(serviceName, allInstances);
                    LOGGER.info("load serviceData : {}/{}", serviceName, allInstances);

                    //更新当前的实例列表
                    namingService.subscribe(serviceName, e -> {
                        NamingEvent namingEvent = (NamingEvent) e;
                        LOGGER.info("subscribe serviceName: {}, event: {}", namingEvent.getServiceName(), namingEvent);
                        serviceInstances.put(serviceName, namingEvent.getInstances());
                        notifyObservers();
                    });
                }

                if (servicesOfServer.getCount() < PAGE_SIZE) {
                    break;
                }
                ++page;
            } catch (Exception e) {
                LOGGER.error("load cluster info err", e);
                break;
            }
        }
    }


    public static void main(String[] args) throws Exception {
        RpcService rpcService = RpcServiceServant.getService("login");
        RpcClient rpcClient = rpcService.getRpcClient(BalanceType.Hash, "userId_011");

        Any params = Any.pack(StringValue.of("rpcClient"));
        Rpc.RpcRequest rpcRequest = Rpc.RpcRequest.newBuilder().setService("login").setMethod("say").addArgs(params).build();
        CompletableFuture<Rpc.RpcResponse> call = rpcClient.call(rpcRequest);

        //同步获得结果
        Rpc.RpcResponse rpcResponse = call.get();
        Any result = rpcResponse.getResult();
        StringValue unpack = result.unpack(StringValue.class);
        System.out.println(unpack.getValue());

    }

    public List<Instance> getInstance(String name) {
        return null;
    }


}
