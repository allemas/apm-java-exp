package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepClass {
    private static Logger LOGGER = LoggerFactory.getLogger(SleepClass.class.getName());

    public void sleep() throws InterruptedException {
        Thread.sleep(500);
    }
}
