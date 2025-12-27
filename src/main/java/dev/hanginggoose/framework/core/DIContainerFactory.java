package dev.hanginggoose.framework.core;

import dev.hanginggoose.framework.graph.DependencyGraphBuilder;
import dev.hanginggoose.framework.scanning.ComponentScanner;
import dev.hanginggoose.framework.scanning.ConfigurationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DIContainerFactory {
    private static final Logger logger = LoggerFactory.getLogger(DIContainerFactory.class);

    public static DIContainer create(String basePackage) {
        logger.info("Creating DI Container for package: {}", basePackage);

        ComponentScanner componentScanner = new ComponentScanner();
        var components = componentScanner.scan(basePackage);

        ConfigurationScanner configScanner = new ConfigurationScanner();
        Map<Class<?>, List<BeanInfo>> beanMethods = configScanner.scanAllBeanMethods(basePackage);

        Map<Class<?>, Object> configInstances = instantiateConfigurations(beanMethods.keySet());
        List<BeanInfo> completeBeanInfos = createCompleteBeanInfos(beanMethods, configInstances);

        for (BeanInfo beanInfo : completeBeanInfos) {
            components.add(beanInfo.getBeanClass());
        }

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(components, completeBeanInfos);

        return new DIContainer(dependencyGraph, completeBeanInfos);
    }

    public static DIContainer create(String[] basePackages) {
        logger.info("Creating DI Container for packages: {}", Arrays.toString(basePackages));

        ComponentScanner componentScanner = new ComponentScanner();
        Set<Class<?>> allComponents = new HashSet<>();

        ConfigurationScanner configScanner = new ConfigurationScanner();
        Map<Class<?>, List<BeanInfo>> allBeanMethods = new HashMap<>();

        for (String pkg : basePackages) {
            allComponents.addAll(componentScanner.scan(pkg));
            allBeanMethods.putAll(configScanner.scanAllBeanMethods(pkg));
        }

        Map<Class<?>, Object> configInstances = instantiateConfigurations(allBeanMethods.keySet());
        List<BeanInfo> completeBeanInfos = createCompleteBeanInfos(allBeanMethods, configInstances);

        for (BeanInfo beanInfo : completeBeanInfos) {
            allComponents.add(beanInfo.getBeanClass());
        }

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(allComponents, completeBeanInfos);

        return new DIContainer(dependencyGraph, completeBeanInfos);
    }

    public static Map<Class<?>, Object> instantiateConfigurations(Set<Class<?>> configClasses) {
        Map<Class<?>, Object> configInstances = new HashMap<>();

        for (Class<?> configClass : configClasses) {
            try {
                Object instance = configClass.getDeclaredConstructor().newInstance();
                configInstances.put(configClass, instance);
                logger.debug("Instantiated configuration class: {}", configClass.getSimpleName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate configuration class: " + configClass.getName(), e);
            }
        }

        return configInstances;
    }

    public static List<BeanInfo> createCompleteBeanInfos(
            Map<Class<?>, List<BeanInfo>> beanMethods,
            Map<Class<?>, Object> configInstances) {
        List<BeanInfo> completeBeanInfos = new ArrayList<>();

        for (var entry : beanMethods.entrySet()) {
            Class<?> configClass = entry.getKey();
            Object configInstance = configInstances.get(configClass);

            for (BeanInfo beanInfo : entry.getValue()) {
                BeanInfo completeBeanInfo = new BeanInfo(
                        beanInfo.getName(),
                        beanInfo.getBeanClass(),
                        beanInfo.getFactoryMethod(),
                        configInstance
                );
                completeBeanInfos.add(completeBeanInfo);
                logger.debug("Created complete BeanInfo for bean: {} from configuration: {}",
                        completeBeanInfo.getName(), configClass.getSimpleName());
            }
        }

        return completeBeanInfos;
    }
}