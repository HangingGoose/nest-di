package dev.hanginggoose.nestdi.framework.core;

import dev.hanginggoose.nestdi.framework.annotations.components.Controller;
import dev.hanginggoose.nestdi.framework.annotations.InputMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

                logger.debug("Registered command '{}' to method '{}.{}' with {} parameters",
                        command, controllerClass.getSimpleName(), method.getName(),
                        method.getParameterCount());
            }
        }
    }

    public void startConsole() {
        Scanner scanner = new Scanner(System.in);
        logger.info("Console dispatcher started. Type 'exit' to quit.");
        logger.info("Available commands: {}", String.join(", ", commands.keySet()));

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if ("exit".equals(input)) {
                logger.info("Exiting console dispatcher.");
                break;
            }

            String[] parts = parseCommand(input);
            String commandName = parts[0].toLowerCase();
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            Method method = commands.get(commandName);
            if (method == null) {
                logger.warn("Unknown command '{}'. Available commands: \n{}",
                        commandName, String.join("\n", commands.keySet()));
                continue;
            }

            try {
                Parameter[] parameters = method.getParameters();

                if (parameters.length != args.length) {
                    logger.error("Wrong number of arguments for command '{}': got {}, expected {}",
                            commandName, args.length, parameters.length);
                    logger.error("Usage: {} {}", commandName,
                            Arrays.stream(parameters)
                                    .map(p -> "<" + p.getType().getSimpleName() + ">")
                                    .reduce((a, b) -> a + " " + b)
                                    .orElse(""));
                    continue;
                }

                Object[] parameterValues = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    parameterValues[i] = convertArgument(args[i], parameters[i].getType());
                }

                Object controllerInstance = container.getBean(method.getDeclaringClass());
                Object result = method.invoke(controllerInstance, parameterValues);
                logger.info("Command '{}' executed successfully.\nResult: {}", commandName, result);
            } catch (Exception e) {
                logger.error("Error executing command '{}': {}", commandName, e.getMessage());
            }
        }

        scanner.close();
    }

    private String[] parseCommand(String input) {
        List<String> parts = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (m.find()) {
            parts.add(m.group(1).replace("\"", ""));
        }
        return parts.toArray(new String[0]);
    }

    private Object convertArgument(String arg, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return arg;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(arg);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(arg);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(arg);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(arg);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(arg);
            } else if (targetType == UUID.class) {
                return UUID.fromString(arg);
            } else if (targetType.isEnum()) {
                @SuppressWarnings("unchecked")
                Enum<?> value = Enum.valueOf((Class<? extends Enum>) targetType, arg.toUpperCase());
                return value;
            } else {
                throw new IllegalArgumentException("Cannot convert to type: " + targetType.getName());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert argument '" + arg +
                    "' to type " + targetType.getSimpleName() + ": " + e.getMessage());
        }
    }

    public Map<String, Method> getRegisteredCommands() {
        return commands;
    }
}