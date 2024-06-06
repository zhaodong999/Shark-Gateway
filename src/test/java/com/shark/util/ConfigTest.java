package com.shark.util;

import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;

public class ConfigTest {

    @Test
    public void test(){
        ResourceBundle sharkBundle = ResourceBundle.getBundle("shark");
        System.out.println(sharkBundle.getString("aa"));
    }
}
