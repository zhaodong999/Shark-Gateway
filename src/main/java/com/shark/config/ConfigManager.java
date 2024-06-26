package com.shark.config;

import java.util.ResourceBundle;

public class ConfigManager {

    private static ConfigManager instance;

    /* 配置文件名称 */
    private final static String CONF_FILE = "config";

    /* zookeeper 连接串*/
    private final static String SRVICE_REGISTRATION_ADDRESS = "service.registration.address";

    /* 服务监听端口*/
    private final static String SERVER_PORT = "server.port";

    private final ResourceBundle configBundle;

    private ConfigManager() {
        configBundle = ResourceBundle.getBundle(CONF_FILE);
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }

        return instance;
    }

    public String getServiceRegistrationAddr() {
        return configBundle.getString(SRVICE_REGISTRATION_ADDRESS);
    }

    public int getServerPort() {
        return Integer.parseInt(configBundle.getString(SERVER_PORT));
    }
}
