package dev.hanginggoose.framework.test.core;

import dev.hanginggoose.demo.components.DemoController;
import dev.hanginggoose.framework.core.ControllerDispatcher;
import dev.hanginggoose.framework.core.DIContainer;
import dev.hanginggoose.framework.core.DIContainerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControllerDispatcherTest {

    private DIContainer container;
    private ControllerDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        container = DIContainerFactory.create("dev.hanginggoose.demo");
        container.start();
        dispatcher = new ControllerDispatcher(container);
    }

    @Test
    void shouldRegisterControllerMethods() {
        assertNotNull(container);
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("greet")).findFirst().orElse(null));
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("process")).findFirst().orElse(null));
    }

    @Test
    void shouldExecuteCommand() {
        DemoController controller = container.getBean(DemoController.class);
        assertNotNull(controller);

        String result = controller.handleRequest();
        assertTrue(result.contains("Controller: Processed data: Data from repository"));
    }
}
