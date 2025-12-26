package dev.hanginggoose.framework.core;

import dev.hanginggoose.framework.annotations.Autowired;
import dev.hanginggoose.framework.graph.DependencyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class DIContainer {
    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final DependencyGraph dependencyGraph;

    public DIContainer(DependencyGraph dependencyGraph) {
        this.dependencyGraph = dependencyGraph;
        logger.info("DI Container initialized");
    }

    public void start() {
        if (dependencyGraph.hasCycles()) {
            throw new IllegalStateException("Cannot start DI Container: Cyclic dependencies detected");
        }

        logger.info("Starting DI Container...");

        List<Class<?>> instantiationOrder = dependencyGraph.getTopologicalOrder();

        for (Class<?> componentClass : instantiationOrder) {
            getBean(componentClass);
        }

        logger.info("DI Container started successfully. Managed beans: {}", instances.size());
    }

    public <T> T getBean(Class<T> beanClass) {
        logger.debug("Requesting bean {}", beanClass.getSimpleName());

        if (instances.containsKey(beanClass)) {
            logger.debug("Returning cached instance of {}", beanClass.getSimpleName());
            return (T) instances.get(beanClass);
        }

        T instance = createInstance(beanClass);
        instances.put(beanClass, instance);

        logger.debug("Created new instance of {}", beanClass.getSimpleName());
        return instance;
    }

    private <T> T createInstance(Class<T> componentClass) {
        logger.debug("Creating instance of {}", componentClass.getSimpleName());

        try {
            Constructor<?>[] constructors = componentClass.getDeclaredConstructors();
            Constructor<?> constructor = dependencyGraph.findConstructor(constructors);

            if (constructor == null) {
                throw new IllegalStateException("Cannot find suitable constructor for " + componentClass.getName());
            }

            Parameter[] parameters = constructor.getParameters();
            Object[] parameterValues = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> parameterType = parameter.getType();

                Optional<Class<?>> dependencyClass = dependencyGraph.findComponentByType(parameterType, dependencyGraph.getGraph().vertexSet());

                if (dependencyClass.isPresent()) {
                    parameterValues[i] = getBean(dependencyClass.get());
                    logger.debug("  Injected {} into {}",
                            dependencyClass.get().getSimpleName(), componentClass.getSimpleName());
                } else {
                    if (isAutowiredRequired(constructor)) {
                        throw new IllegalStateException(
                                String.format("No component found for required dependency %s in %s",
                                        parameterType.getName(), componentClass.getName())
                        );
                    } else {
                        parameterValues[i] = null;
                        logger.warn("Optional dependency not found: {} in {}",
                                parameterType.getSimpleName(), componentClass.getSimpleName());
                    }
                }
            }

            constructor.setAccessible(true);
            T instance = (T) constructor.newInstance(parameterValues);

            logger.info("Successfully created instance of {}", componentClass.getSimpleName());
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to create instance of " + componentClass.getName(), exception);
        }
    }

    private boolean isAutowiredRequired(Constructor<?> constructor) {
        Autowired autowired = constructor.getAnnotation(Autowired.class);
        return autowired == null || autowired.required();
    }

    public Map<Class<?>, Object> getAllBeans() {
        return Collections.unmodifiableMap(instances);
    }

    public void shutdown() {
        logger.info("Shutting down DI Container...");
        instances.clear();
        logger.info("DI Container shut down");
    }
}