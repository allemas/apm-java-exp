package org.allemas;

import javassist.*;
import org.allemas.transformers.ATMTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class Premain {
    private static Logger logger = LoggerFactory.getLogger(Premain.class.getName());

    private static Instrumentation instrumentation;

    /**
     * JVM hook to dynamically load javaagent at runtime.
     * <p>
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args
     * @param inst
     * @throws Exception
     */
    public static void agentmain(String args, Instrumentation inst) {
        logger.info("AGENT : Starting...");
        String classNameTransform = "org.example.utils.SleepClass";
        Class<?> targetCls = null;

        try {
            targetCls = Class.forName(classNameTransform);
            ClassLoader targetClassLoader = targetCls.getClassLoader();

            logger.info("AGENT : Transformer Starting...");
            ATMTransformer dt = new ATMTransformer(
                    targetCls.getName(), targetClassLoader);

            inst.addTransformer(dt, true);
            inst.retransformClasses(targetCls);

            logger.info("AGENT : retransform...");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static void premain(
            String agentArgs, Instrumentation inst) {
        logger.info("[APM] Loaded");


        logger.info("[APM] Showing all class loaded...");
        for (Class aClass : inst.getAllLoadedClasses()) {
            logger.info("[APM] " + aClass.getName());
        }
        String classNameTransform = "org.example.utils.SleepClass";
        Class<?> targetCls = null;

        try {
            targetCls = Class.forName(classNameTransform);
            ClassLoader targetClassLoader = targetCls.getClassLoader();

            ATMTransformer dt = new ATMTransformer(
                    targetCls.getName(), targetClassLoader);

            inst.addTransformer(dt, false);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}
