package dev.hanginggoose.nestdi.framework.graph;

import dev.hanginggoose.nestdi.framework.annotations.Configuration;
import dev.hanginggoose.nestdi.framework.core.BeanInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class DependencyGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

    public DependencyGraph build(Set<Class<?>> components, List<BeanInfo> beanInfos) {
        logger.debug("Building dependency graph for {} components and {} beans",
                components.size(), beanInfos.size());

        DependencyGraph dependencyGraph = new DependencyGraph();

        for (Class<?> component : components) {
            dependencyGraph.addComponent(component);
        }

        for (BeanInfo beanInfo : beanInfos) {
            dependencyGraph.addComponent(beanInfo.getBeanClass());
        }

        for (Class<?> component : components) {
            if (!component.isAnnotationPresent(Configuration.class)) {
                dependencyGraph.analyzeDependencies(component, components);
            }
        }

        for (BeanInfo beanInfo : beanInfos) {
            dependencyGraph.analyzeBeanDependencies(beanInfo, components);
        }

        if (dependencyGraph.hasCycles()) {
            logger.error("Cyclic dependencies detected in dependency graph: ");
            dependencyGraph.getCycles().forEach(cycleVertex ->
                    logger.error("  - {}", cycleVertex.getSimpleName()));
        } else {
            logger.debug("No cyclic dependencies detected");
        }

        dependencyGraph.printGraph();

        return dependencyGraph;
    }
}