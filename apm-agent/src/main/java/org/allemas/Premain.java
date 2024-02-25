package org.allemas;

import javassist.*;
import org.allemas.transformers.ATMTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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

                    /**
                     * Remove the duration and sleep, and the profiler takes the hand on the runtime
                     */
                    while (true) {
                        List<String> strace = new ArrayList<>();

                        Thread.getAllStackTraces().forEach(((thread, stackTraceElements) ->
                        {
                            List<String> trace = Stream.of(stackTraceElements).map(
                                    (stackTraceElement -> (stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName()).intern())
                            ).toList();

                            strace.addAll(trace);
                        }));
                        logger.info("AGENT APM Stacktace: " + strace);

                        Thread.sleep(1000);

                    }


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.setDaemon(true);
        t.setName("stacktrance");
        t.start();

        Thread jmxMetrics = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    long heapMemory = Runtime.getRuntime().availableProcessors();
                    long heapMemory2 = Runtime.getRuntime().freeMemory();

                    logger.info("AGENT APM : JMX processors available : " + heapMemory + " and freememory is : "+heapMemory2);
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });

        jmxMetrics.setDaemon(true);
        jmxMetrics.setName("profiler");
        jmxMetrics.start();


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
//https://github.com/parttimenerd/tiny-profiler/tree/main