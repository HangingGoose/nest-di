package dev.hanginggoose.framework.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DependencyGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);

    public DependencyGraph build(Set<Class<?>> components) {
        logger.debug("Building dependency graph for {} components", components.size());

        DependencyGraph dependencyGraph = new DependencyGraph();

        for (Class<?> component : components) {
            dependencyGraph.addComponent(component);
        }

        for (Class<?> component : components) {
            dependencyGraph.analyzeDependencies(component, components);
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