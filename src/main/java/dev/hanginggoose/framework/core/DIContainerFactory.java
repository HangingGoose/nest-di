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

        ComponentScanner scanner = new ComponentScanner();
        var components = scanner.scan(basePackage);

        ConfigurationScanner configScanner = new ConfigurationScanner();
        Map<Class<?>, List<BeanInfo>> beanMethods = configScanner.scanAllBeanMethods(basePackage);
        beanMethods.values().stream()
                .flatMap(List::stream)
                .forEach(beanInfo -> components.add(beanInfo.getBeanClass()));

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(components);

        Map<Class<?>, Object> configInstances = instantiateConfigurations(beanMethods.keySet());
        List<BeanInfo> completeBeanInfos = createCompleteBeanInfos(beanMethods, configInstances);

        return new DIContainer(dependencyGraph, completeBeanInfos);
    }

    public static DIContainer create(String[] basePackages) {
        logger.info("Creating DI Container for packages: {}", Arrays.toString(basePackages));

        ComponentScanner scanner = new ComponentScanner();
        var components = scanner.scanPackages(basePackages);

        ConfigurationScanner configScanner = new ConfigurationScanner();
        Map<Class<?>, List<BeanInfo>> beanMethods = new HashMap<>();
        for (String pkg : basePackages) {
            Map<Class<?>, List<BeanInfo>> packageBeans = configScanner.scanAllBeanMethods(pkg);
            beanMethods.putAll(packageBeans);
        }

        beanMethods.values().stream()
                .flatMap(List::stream)
                .forEach(beanInfo -> components.add(beanInfo.getBeanClass()));

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(components);

        Map<Class<?>, Object> configInstances = instantiateConfigurations(beanMethods.keySet());
        List<BeanInfo> completeBeanInfos = createCompleteBeanInfos(beanMethods, configInstances);

        return new DIContainer(dependencyGraph, completeBeanInfos);
    }

    private static Map<Class<?>, Object> instantiateConfigurations(Set<Class<?>> configClasses) {
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

    private static List<BeanInfo> createCompleteBeanInfos(
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