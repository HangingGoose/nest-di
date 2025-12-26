package dev.hanginggoose.demo;

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

            logger.info("Response: {}", response);

            logger.info("Managed beans:");
            container.getAllBeans().forEach((clazz, instance) ->
                    logger.info("  - {}: {}", clazz.getSimpleName(), instance)
            );

            container.shutdown();
        } catch (Exception e) {
            logger.error("Error in demo application", e);
        }

        logger.info("Demo finished");
    }
}