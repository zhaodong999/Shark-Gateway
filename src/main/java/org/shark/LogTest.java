package org.shark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(LogTest.class);

    public void testLog(){
        LOGGER.info("test=====>>>>>> ok <<<<========");
    }

    public static void main(String[] args) {
        LogTest logTest = new LogTest();
        logTest.testLog();
    }
}
