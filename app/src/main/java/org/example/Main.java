package org.example;

import org.example.utils.SleepClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 100; i++) {
            try {

                SleepClass s = new SleepClass();
                logger.info("[APP] Starting the application");
                s.sleep();
                if (i % 3 == 0) {


                    throw new RuntimeException("Runtime Test Exception ! ");

                }
                logger.info("[APP] End of sleeping...");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}