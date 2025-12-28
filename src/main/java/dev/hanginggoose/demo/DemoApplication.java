package dev.hanginggoose.demo;

import dev.hanginggoose.demo.components.BeanUser;
import dev.hanginggoose.demo.components.InterceptedController;
import dev.hanginggoose.framework.core.ControllerDispatcher;
import dev.hanginggoose.framework.core.DIContainer;
import dev.hanginggoose.framework.core.DIContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        logger.info("Starting DI Container Demo");

        try {
            DIContainer container = DIContainerFactory.create("dev.hanginggoose.demo");
            container.start();

            container.getAllBeans().forEach((clazz, instance) ->
                    logger.debug("Bean: {} -> {}", clazz.getSimpleName(), instance)
            );

            ControllerDispatcher dispatcher = new ControllerDispatcher(container);
            dispatcher.startConsole();

            String greeting = (String) container.getBean("customGreeting");
            logger.info("Custom greeting bean: {}", greeting);

            Integer answer = container.getBean(Integer.class);
            logger.info("Custom answer bean: {}", answer);

            BeanUser beanUser = container.getBean(BeanUser.class);
            logger.info("BeanUser says: {}", beanUser.useBeans());

            InterceptedController interceptedController = container.getBean(InterceptedController.class);
            logger.info("InterceptedController result: {}",
                    interceptedController.interceptedMethod()
            );

            logger.info("Managed beans ({}):", container.getAllBeans().size());

            container.shutdown();
        } catch (Exception e) {
            logger.error("Error in demo application", e);
        }

        logger.info("Demo finished");
    }
}