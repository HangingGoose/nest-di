package dev.hanginggoose.nestdi.demo;

import dev.hanginggoose.nestdi.demo.beandemo.BeanUser;
import dev.hanginggoose.nestdi.framework.core.ControllerDispatcher;
import dev.hanginggoose.nestdi.framework.core.DIContainer;
import dev.hanginggoose.nestdi.framework.core.DIContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        logger.info("Starting DI Container Demo");

        try {
            DIContainer container = DIContainerFactory.create("dev.hanginggoose.nestdi.demo");
            container.start();

            container.getAllBeans().forEach((clazz, instance) ->
                    logger.debug("Bean: {} -> {}", clazz.getSimpleName(), instance)
            );

            ControllerDispatcher dispatcher = new ControllerDispatcher(container);
            dispatcher.startConsole();

            BeanUser beanUser = container.getBean(BeanUser.class);
            logger.info("BeanUser says: {}", beanUser.useBeans());

            logger.info("Managed beans ({}):", container.getAllBeans().size());

            container.shutdown();
        } catch (Exception e) {
            logger.error("Error in demo application", e);
        }

        logger.info("Demo finished");
    }
}