# My own APM - Application Performance Monitor

_This project is not for applications in production, **it's juste an expériment**_


## WHY ?
First of all, discover and build APM for classic java applications. 
After why not play at stream data to APM server, in funny serial formats
and in the last part focus on time spend in I/O and user sessions (oauth).


## How start an agent ? 

[This](https://www.baeldung.com/java-instrumentation) describe very well the concept of agent injected into a java app what you can achieve by injecting
bytecode in JVM.

For this 2 running mode : 
- Static load modifies the byte-code at startup time before the application ran : `java -javaagent:<agent>.jar app.jar `
- Dynamic load java agent is load into a running JVM and attached by the [JAVA API](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.attach/module-summary.html) 
  - Start the app :  `java -jar app.jar  <mainclass>` 
  - attach the different agent after :  `java -jar agent.jar  <mainclass>` 

## Lets go ! 

We start with a static load (premain) java agent injected by `java -javaagent:<agent>.jar` flag. If you're familiar with glowroot it's certainly how you run it.
In this expériment we will use `Jassist` [doc](https://www.baeldung.com/javassist) lib to change bytecode.

[java.lang.instrument](https://docs.oracle.com/en/java/javase/17/docs/api/java.instrument/java/lang/instrument/Instrumentation.html) interface lets us manipulate class loaded and know crucial information about the app.
- which class are loaded in the JVM 
- add/remove tranformers
- redefine class : you can replace a class (: 

## Exp1
We start by creating an application in which a Thread.sleep(infinite) is made and we will link an agent who show which class are loaded.
```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Hello world!");
        Thread.sleep(50000);
    }
}
```

When a JVM is launched in a way that indicates an agent class. In that case an Instrumentation instance is passed to the premain method of the agent class.

To do that you can create a multi module maven project without forget Premain-Class clause, if you use maven plugin you can add in the pom.xml :
```java

  <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.1.1</version>

                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifestEntries>
                            <Premain-Class>org.allemas.Premain</Premain-Class>
                        </manifestEntries>
                        <manifest>
                            <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                            <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
                        </manifest>
                    </archive>
                </configuration>

                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>

            </plugin>
        </plugins>
    </build>
```

Once the Premain class is declared in the Manifest.
We can get all the class loaded by the JVM with this piece of code 
````java
    public static void premain(
            String agentArgs, Instrumentation inst) {
        logger.info("[APM] Loaded");

        logger.info("[APM] Showing all class loaded...");
        for (Class aClass : inst.getAllLoadedClasses()) {
            logger.info("[APM] " + aClass.getName());
        }
    }
````
YAY  ! It's works !
```
12:19:41.568 [main] INFO org.allemas.Premain - [APM] Showing all class loaded...
12:19:41.570 [main] INFO org.allemas.Premain - [APM] sun.text.resources.cldr.ext.FormatData_fr
12:19:41.570 [main] INFO org.allemas.Premain - [APM] sun.util.resources.provider.LocaleDataProvider
12:19:41.570 [main] INFO org.allemas.Premain - [APM] sun.util.resources.cldr.provider.CLDRLocaleDataMetaInfo
12:19:41.570 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.core.CoreConstants
12:19:41.570 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.classic.util.LogbackMDCAdapter
12:19:41.570 [main] INFO org.allemas.Premain - [APM] org.slf4j.impl.StaticMDCBinder
12:19:41.570 [main] INFO org.allemas.Premain - [APM] org.slf4j.spi.MDCAdapter
12:19:41.570 [main] INFO org.allemas.Premain - [APM] org.slf4j.MDC
12:19:41.570 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.classic.spi.EventArgUtil
12:19:41.570 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.classic.spi.IThrowableProxy
12:19:41.570 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.classic.spi.LoggingEvent
12:19:41.570 [main] INFO org.allemas.Premain - [APM] [Lch.qos.logback.core.spi.FilterReply;
12:19:41.571 [main] INFO org.allemas.Premain - [APM] ch.qos.logback.core.spi.FilterReply

```


## EXP 2
I tried to change bytes code of a specific class. But this class is not loaded at the time I want to add bytescode.
To do that, I need an external agent. The agent will be attached to the VM with the Java Instrument API.
In short hands, we search je pid of the java VM, attach to it and inject the agent while the app jar running.

once the Jar is attached, the agent interface run this piece of code : 
```java
 public static void agentmain(String args, Instrumentation inst){
        ...
  }
```

and its here we can alter the Java Bytecode directly in the JVM.
Simply I added a "BEFORE" println the methode is triggered.

```java
                ClassPool pool = ClassPool.getDefault();
                pool.appendClassPath(new LoaderClassPath(loader));
                
                CtClass ctClass = pool.get(classNameTransform);
                CtMethod method = ctClass.getDeclaredMethod("sleep"); <------------------
                method.insertBefore("System.out.println(\"BEFOOORE\");"); // <-----------

                classfileBuffer = ctClass.toBytecode();
                ctClass.detach();

                return classfileBuffer;
```

We now can launch the app main :
```java
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            SleepClass s = new SleepClass();
            logger.info("[APP] Starting the application");
            logger.info("[APP] Thread sleep for 500");
            s.sleep(); // <------------ the methode we want alter
            logger.info("[APP] End of sleeping...");
        }
    }
```
the application write to the stdout : 
```
17:11:48.499 [main] INFO org.example.Main - [APP] Starting the application
17:11:48.499 [main] INFO org.example.Main - [APP] Thread sleep for 5000
17:11:49.002 [main] INFO org.example.Main - [APP] End of sleeping...
```
And after we launched the agent : 
``` 
BEFOOORE
17:12:24.869 [main] INFO org.example.Main - [APP] End of sleeping...
17:12:24.869 [main] INFO org.example.Main - [APP] Starting the application
17:12:24.869 [main] INFO org.example.Main - [APP] Thread sleep for 5000
BEFOOORE
17:12:25.374 [main] INFO org.example.Main - [APP] End of sleeping...
17:12:25.374 [main] INFO org.example.Main - [APP] Starting the application
17:12:25.374 [main] INFO org.example.Main - [APP] Thread sleep for 5000
```

