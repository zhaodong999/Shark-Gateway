package com.shark.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.junit.jupiter.api.Test;

public class NacosServiceTest {

    @Test
    public void test() throws NacosException, InterruptedException {
        NamingService namingService = NamingFactory.createNamingService("localhost:8848");

        namingService.registerInstance("shark.logic", "localhost", 8889);
        Thread.currentThread().join();
    }
}
