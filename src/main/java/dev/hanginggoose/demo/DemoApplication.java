package dev.hanginggoose.demo;

import dev.hanginggoose.demo.components.BeanUser;
import dev.hanginggoose.demo.components.DemoController;
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

            DemoController controller = container.getBean(DemoController.class);
            String response = controller.handleRequest();
            logger.info("Controller response: {}", response);

            String greeting = (String) container.getBean("customGreeting");
            logger.info("Custom greeting bean: {}", greeting);

            Integer answer = container.getBean(Integer.class);
            logger.info("Custom answer bean: {}", answer);

            BeanUser beanUser = container.getBean(BeanUser.class);
            logger.info("BeanUser says: {}", beanUser.useBeans());

            logger.info("Managed beans ({}):", container.getAllBeans().size());
            container.getAllBeans().forEach((clazz, instance) ->
                    logger.info("  - {}: {}", clazz.getSimpleName(), instance)
            );

            logger.info("Named beans ({}):", container.getBeanNames().size());
            container.getBeanNames().forEach(name ->
                    logger.info("  - {}: {}", name, container.getBean(name))
            );

            container.shutdown();
        } catch (Exception e) {
            logger.error("Error in demo application", e);
        }

        logger.info("Demo finished");
    }
}