package dev.hanginggoose.nestdi.framework.scanning;

import dev.hanginggoose.nestdi.framework.annotations.components.*;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.reflect.Modifier.isAbstract;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

public class ComponentScanner {

    private static final Logger logger = LoggerFactory.getLogger(ComponentScanner.class);
    private final Map<String, Set<Class<?>>> cache = new ConcurrentHashMap<>();

    public Set<Class<?>> scan(String basePackage) {
        logger.debug("Scanning for components in package {}", basePackage);

        if (cache.containsKey(basePackage)) {
            logger.debug("Returning cached components for package: {}", basePackage);
            return cache.get(basePackage);
        }

        Reflections reflections = new Reflections(basePackage);

        Set<Class<?>> components = new HashSet<>();
        components.addAll(reflections.get(SubTypes.of(TypesAnnotated.with(Component.class)).asClass()));
        components.addAll(reflections.get(SubTypes.of(TypesAnnotated.with(Repository.class)).asClass()));
        components.addAll(reflections.get(SubTypes.of(TypesAnnotated.with(Service.class)).asClass()));
        components.addAll(reflections.get(SubTypes.of(TypesAnnotated.with(Controller.class)).asClass()));
        components.addAll(reflections.get(SubTypes.of(TypesAnnotated.with(Configuration.class)).asClass()));

        components.removeIf(clazz -> clazz.isInterface() || isAbstract(clazz.getModifiers()));

        logger.debug("Found {} components in package {}:", components.size(), basePackage);
        components.forEach(clazz ->
                logger.debug("  - {} ({})", clazz.getName(), getComponentAnnotationType(clazz))
        );

        cache.put(basePackage, components);

        return components;
    }

    public Set<Class<?>> scanPackages(String[] packages) {
        Set<Class<?>> allComponents = new HashSet<>();

        for (String pkg : packages) {
            allComponents.addAll(scan(pkg));
        }

        return allComponents;
    }

    public void clearCache() {
        cache.clear();
        logger.debug("Scanner cache cleared");
    }

    private String getComponentAnnotationType(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Controller.class)) return "@Controller";
        if (clazz.isAnnotationPresent(Service.class)) return "@Service";
        if (clazz.isAnnotationPresent(Repository.class)) return "@Repository";
        if (clazz.isAnnotationPresent(Configuration.class)) return "@Configuration";
        if (clazz.isAnnotationPresent(Component.class)) return "@Component";
        return "Unknown";
    }
}