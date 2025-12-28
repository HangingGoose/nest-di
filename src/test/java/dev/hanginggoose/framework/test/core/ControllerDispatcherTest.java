package dev.hanginggoose.framework.test.core;

import dev.hanginggoose.framework.core.ControllerDispatcher;
import dev.hanginggoose.framework.core.DIContainer;
import dev.hanginggoose.framework.core.DIContainerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void shouldRegisterAnnotatedControllerMethods() {
        assertNotNull(container);
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("addbrand")).findFirst().orElse(null));
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("brandproducts")).findFirst().orElse(null));
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("getbrand")).findFirst().orElse(null));
        assertNotNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("addproduct")).findFirst().orElse(null));
    }

    @Test
    void shouldNotRegisterNonAnnotatedControllerMethods() {
        assertNotNull(container);
        assertNull(dispatcher.getRegisteredCommands().keySet()
                .stream().filter(command -> command.equals("getProduct")).findFirst().orElse(null));
    }
}