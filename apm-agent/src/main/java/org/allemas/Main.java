package org.allemas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);


    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        logger.info("Main started");
        AgentVMMonitor.launch();


    }

}
