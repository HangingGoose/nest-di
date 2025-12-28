package dev.hanginggoose.nestdi.framework.scanning;

import dev.hanginggoose.nestdi.framework.annotations.Bean;
import dev.hanginggoose.nestdi.framework.annotations.Configuration;
import dev.hanginggoose.nestdi.framework.core.BeanInfo;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

import static java.lang.reflect.Modifier.isAbstract;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ConfigurationScanner {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationScanner.class);

    public Set<Class<?>> scanConfigurations(String basePackage) {
        logger.debug("Scanning for configuration classes in package {}", basePackage);

        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> configurations = new HashSet<>(reflections.get(SubTypes.of(TypesAnnotated.with(Configuration.class)).asClass()));

        configurations.removeIf(clazz -> clazz.isInterface() || isAbstract(clazz.getModifiers()));

        logger.debug("Found {} configuration classes in package {}:", configurations.size(), basePackage);
        configurations.forEach(clazz ->
                logger.debug("  - {}", clazz.getName())
        );

        return configurations;
    }

    public List<BeanInfo> scanBeanMethods(Class<?> configClass) {
        logger.debug("Scanning for bean methods in configuration class {}", configClass.getSimpleName());

        List<BeanInfo> beanInfos = new ArrayList<>();

        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                Bean annotation = method.getAnnotation(Bean.class);
                String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
                Class<?> returnType = method.getReturnType();

                BeanInfo beanInfo = new BeanInfo(name, returnType, method, null);
                beanInfos.add(beanInfo);

                logger.debug("  Found bean method: {} returning {} (name: {})",
                        method.getName(), returnType.getSimpleName(), name);
            }
        }

        return beanInfos;
    }

    public Map<Class<?>, List<BeanInfo>> scanAllBeanMethods(String basePackage) {
        Set<Class<?>> configurations = scanConfigurations(basePackage);
        Map<Class<?>, List<BeanInfo>> allBeanMethods = new HashMap<>();

        for (Class<?> configuration : configurations) {
            List<BeanInfo> beanInfos = scanBeanMethods(configuration);
            if (!beanInfos.isEmpty()) {
                allBeanMethods.put(configuration, beanInfos);
            }
        }

        return allBeanMethods;
    }
}