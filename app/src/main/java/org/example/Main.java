package org.example;

import org.example.utils.SleepClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            SleepClass s = new SleepClass();
            logger.info("[APP] Starting the application");
            logger.info("[APP] Thread sleep for 5000");
            s.sleep();
            logger.info("[APP] End of sleeping...");
        }
    }
}