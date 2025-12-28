package dev.hanginggoose.framework.core;

import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.InputMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ControllerDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(ControllerDispatcher.class);
    private final Map<String, Method> commands = new HashMap<>();
    private final DIContainer container;

    public ControllerDispatcher(DIContainer container) {
        this.container = container;
        discoverControllers();
    }

    private void discoverControllers() {
        container.getBeansWithAnnotation(Controller.class)
                .forEach(this::registerControllerMethods);

        logger.info("Total commands registered: {}", commands.size());
    }

    private void registerControllerMethods(Class<?> controllerClass, Object instance) {
        logger.debug("Discovering methods in controller: {}", controllerClass.getSimpleName());

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(InputMapping.class)) {
                InputMapping mapping = method.getAnnotation(InputMapping.class);
                String command = mapping.value().isEmpty() ? method.getName() : mapping.value();
                commands.put(command.trim().toLowerCase(), method);
                logger.debug("Registered command '{}' to method '{}.{}'",
                        command, controllerClass.getSimpleName(), method.getName());
            }
        }
    }

    public void startConsole() {
        Scanner scanner = new Scanner(System.in);
        logger.info("Console dispatcher started. Type 'exit' to quit.");
        logger.info("Available commands: {}", String.join(", ", commands.keySet()));

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.isEmpty()) {
                continue;
            }

            if ("exit".equals(input)) {
                logger.info("Exiting console dispatcher.");
                break;
            }

            Method method = commands.get(input);
            if (method == null) {
                logger.warn("Unknown command. Available commands: \n{}",
                        String.join("\n", commands.keySet()));
                continue;
            }

            try {
                Object controllerInstance = container.getBean(method.getDeclaringClass());
                Object result = method.invoke(controllerInstance);
                logger.info("Command '{}' executed successfully. Result: {}", input, result);
            } catch (Exception e) {
                logger.error("Error executing command '{}': {}", input, e.getMessage());
            }
        }

        scanner.close();
    }

    public Map<String, Method> getRegisteredCommands() {
        return commands;
    }
}
