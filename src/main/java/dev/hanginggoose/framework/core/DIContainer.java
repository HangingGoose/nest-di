package dev.hanginggoose.framework.core;

import dev.hanginggoose.framework.annotations.Autowired;
import dev.hanginggoose.framework.graph.DependencyGraph;
import dev.hanginggoose.framework.interception.InterceptionProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DIContainer {
    private static final Logger logger = LoggerFactory.getLogger(DIContainer.class);

    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
    private final Map<String, Object> namedBeans = new ConcurrentHashMap<>();
    private final Map<Class<?>, BeanInfo> beanInfos = new ConcurrentHashMap<>();
    private final DependencyGraph dependencyGraph;
    private final List<BeanInfo> configurationBeans;

    public DIContainer(DependencyGraph dependencyGraph, List<BeanInfo> configurationBeans) {
        this.dependencyGraph = dependencyGraph;
        this.configurationBeans = configurationBeans != null ? configurationBeans : new ArrayList<>();
        logger.info("DI Container initialized with {} configuration beans",
                this.configurationBeans.size());
    }

    public void start() {
        if (dependencyGraph.hasCycles()) {
            throw new IllegalStateException("Cannot start DI Container: Cyclic dependencies detected");
        }

        logger.info("Starting DI Container...");

        List<Class<?>> instantiationOrder = dependencyGraph.getTopologicalOrder();

        for (BeanInfo beanInfo : configurationBeans) {
            beanInfos.put(beanInfo.getBeanClass(), beanInfo);
            logger.debug("Registered BeanInfo for: {}", beanInfo.getBeanClass().getSimpleName());
        }

        for (Class<?> componentClass : instantiationOrder) {
            getBean(componentClass);
        }

        logger.info("DI Container started successfully. Managed beans: {}", instances.size());
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanClass) {
        logger.debug("Requesting bean {}", beanClass.getSimpleName());

        Object existingInstance = instances.get(beanClass);
        if (existingInstance != null) {
            logger.debug("Returning cached instance of {}", beanClass.getSimpleName());
            return (T) existingInstance;
        }

        if (beanInfos.containsKey(beanClass)) {
            return createBeanFromMethod(beanClass);
        }

        Object instance = instances.computeIfAbsent(beanClass,
                key -> createInstance((Class<?>) key));

        logger.debug("Created new instance of {}", beanClass.getSimpleName());
        return (T) instance;
    }

    public Object getBean(String beanName) {
        logger.debug("Requesting bean by name: {}", beanName);

        if (namedBeans.containsKey(beanName)) {
            logger.debug("Returning cached instance of bean named {}", beanName);
            return namedBeans.get(beanName);
        }

        return beanInfos.values().stream()
                .filter(beanInfo -> beanInfo.getName().equals(beanName))
                .findFirst()
                .map(beanInfo -> getBean(beanInfo.getBeanClass()))
                .orElseThrow(() -> new NoSuchElementException("No bean found with name: " + beanName));
    }

    @SuppressWarnings("unchecked")
    private <T> T createBeanFromMethod(Class<T> beanClass) {
        BeanInfo beanInfo = beanInfos.get(beanClass);
        if (beanInfo == null) {
            throw new IllegalArgumentException("No BeanInfo found for: " + beanClass.getName());
        }

        logger.debug("Creating bean {} using factory method {}", beanClass.getSimpleName(),
                beanInfo.getFactoryMethod().getName());

        try {
            Method factoryMethod = beanInfo.getFactoryMethod();
            Object configInstance = beanInfo.getConfigInstance();

            if (configInstance == null) {
                throw new IllegalStateException("No configuration instance available for factory method " +
                        factoryMethod.getName() + " of bean " + beanClass.getName());
            }

            Parameter[] parameters = factoryMethod.getParameters();
            Object[] parameterValues = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Class<?> parameterType = parameter.getType();

                parameterValues[i] = getBean(parameterType);
                logger.debug("  Injected {} into factory method {}",
                        parameterType.getSimpleName(), factoryMethod.getName());
            }

            factoryMethod.setAccessible(true);
            T instance = (T) factoryMethod.invoke(configInstance, parameterValues);
            instance = InterceptionProxyFactory.createProxy(instance);

            if (instance == null) {
                throw new IllegalStateException("Factory method " + factoryMethod.getName() +
                        " returned null for bean " + beanClass.getName());
            }

            instances.put(beanClass, instance);
            if (namedBeans.containsKey(beanInfo.getName())) {
                throw new IllegalStateException("Bean name already exists: " + beanInfo.getName());
            }
            namedBeans.put(beanInfo.getName(), instance);

            logger.info("Successfully created bean {} using factory method {}",
                    beanClass.getSimpleName(), factoryMethod.getName());

            return instance;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create bean " + beanClass.getName() +
                    " using factory method " + beanInfo.getFactoryMethod().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
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
            instance = InterceptionProxyFactory.createProxy(instance);

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

    public Map<Class<?>, Object> getBeansWithAnnotation(Class<? extends Annotation> annotation) {
        Map<Class<?>, Object> result = new HashMap<>();
        instances.keySet().stream().filter(c -> c.isAnnotationPresent(annotation))
                .forEach(c -> result.put(c, instances.get(c)));

        return Collections.unmodifiableMap(result);
    }

    public Map<String, Object> getNamedBeans() {
        return Collections.unmodifiableMap(namedBeans);
    }

    public Set<String> getBeanNames() {
        return Collections.unmodifiableSet(namedBeans.keySet());
    }

    public void shutdown() {
        logger.info("Shutting down DI Container...");
        instances.clear();
        namedBeans.clear();
        beanInfos.clear();
        logger.info("DI Container shut down");
    }
}