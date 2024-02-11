package org.allemas.transformers;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class ATMTransformer implements ClassFileTransformer {

    private static Logger logger = LoggerFactory.getLogger(ATMTransformer.class.getName());

    String classNameTransform;
    ClassLoader targetClassLoader;

    public ATMTransformer(String name, ClassLoader targetClassLoader) {
        classNameTransform = name;
        logger.info("TRANSFORMER : " + name);
        targetClassLoader = targetClassLoader;

    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;

        String finalTargetClassName = this.classNameTransform
                .replaceAll("\\.", "/");


        logger.info("targetClassLoader =>" + loader);

        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }

        if (className.equals(finalTargetClassName)) {
            logger.info("[APM] load ==> " + className);

            try {
                ClassPool pool = ClassPool.getDefault();
                pool.appendClassPath(new LoaderClassPath(loader));

                CtClass ctClass = pool.get(classNameTransform);
                CtMethod method = ctClass.getDeclaredMethod("sleep");
                method.insertBefore("System.out.println(\"BEFOOORE\");");

                classfileBuffer = ctClass.toBytecode();
                ctClass.detach();

                return classfileBuffer;
            } catch (Exception e) {

                logger.error(e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }


        }
        return classfileBuffer;
    }
}
