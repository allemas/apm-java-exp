package org.allemas;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Optional;

public class AgentVMMonitor {
    static final Logger logger = LoggerFactory.getLogger(AgentVMMonitor.class);


    private static final String jarFilePath = "C:\\Users\\sebas\\Documents\\apm\\apm\\apm-agent\\target\\apm-agent-1.0-SNAPSHOT-jar-with-dependencies.jar";


    public static void launch() {
        logger.info(AgentVMMonitor.class.getName() + " start VM");
        String applicationName = "app";

        //iterate all jvms and get the first one that matches our application name
        Optional<String> jvmProcessOpt = Optional.ofNullable(VirtualMachine.list()
                .stream()
                .filter(jvm -> {
                    logger.info("jvm:{}", jvm.displayName());
                    logger.info("jvm:{}", jvm.id());
                    return jvm.displayName().contains(applicationName);
                })
                .findFirst().get().id());

        if (!jvmProcessOpt.isPresent()) {
            logger.error("Target Application not found");
            return;
        }

        File agentFile = new File(jarFilePath);
        String vmName = jvmProcessOpt.get();
        try {
            VirtualMachine currentVM = VirtualMachine.attach(vmName);
            logger.info(AgentVMMonitor.class.getName() + " attach jarfile: " + agentFile.getAbsolutePath());
            currentVM.loadAgent(agentFile.getAbsolutePath());
            currentVM.detach();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
