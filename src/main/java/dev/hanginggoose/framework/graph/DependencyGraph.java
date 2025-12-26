package dev.hanginggoose.framework.graph;

import dev.hanginggoose.framework.annotations.Autowired;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class DependencyGraph {

    private static final Logger logger = LoggerFactory.getLogger(DependencyGraph.class);
    private final SimpleDirectedGraph<Class<?>, DefaultEdge> graph;
    private final CycleDetector<Class<?>, DefaultEdge> cycleDetector;

    public DependencyGraph() {
        this.graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.cycleDetector = new CycleDetector<>(graph);
    }

    public void addComponent(Class<?> componentClass) {
        if (!graph.containsVertex(componentClass)) {
            graph.addVertex(componentClass);
            logger.debug("Added vertex: {}", componentClass.getSimpleName());
        }
    }

    public void analyzeDependencies(Class<?> componentClass, Set<Class<?>> allComponents) {
        Constructor<?>[] constructors = componentClass.getDeclaredConstructors();
        Constructor<?> chosenConstructor = findConstructor(constructors);

        if (chosenConstructor != null) {
            Parameter[] parameters = chosenConstructor.getParameters();
            for (Parameter parameter : parameters) {
                Class<?> dependencyType = parameter.getType();

                Optional<Class<?>> dependency = findComponentByType(dependencyType, allComponents);

                if (dependency.isPresent()) {
                    addDependency(componentClass, dependency.get());
                } else {
                    logger.warn("No component found for dependency type: {} in {}",
                            dependencyType.getSimpleName(),
                            componentClass.getSimpleName());
                }
            }
        }
    }

    private void addDependency(Class<?> source, Class<?> target) {
        if (!graph.containsEdge(source, target)) {
            graph.addEdge(source, target);
            logger.debug("Added edge from {} to {}",
                    source.getSimpleName(),
                    target.getSimpleName());
        }
    }

    public Set<Class<?>> getCycles() {
        return cycleDetector.findCycles();
    }

    public boolean hasCycles() {
        return cycleDetector.detectCycles();
    }

    private Constructor<?> findConstructor(Constructor<?>[] constructors) {
        if (constructors.length == 1) {
            return constructors[0];
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                return constructor;
            }
        }

        return Arrays.stream(constructors)
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElse(null);
    }

    private Optional<Class<?>> findComponentByType(Class<?> type, Set<Class<?>> allComponents) {
        return allComponents.stream()
                .filter(type::isAssignableFrom)
                .findFirst();
    }

    public void printGraph() {
        logger.info("Dependency Graph ({} vertices, {} edges):",
                graph.vertexSet().size(), graph.edgeSet().size());

        for (Class<?> vertex : graph.vertexSet()) {
            logger.info("  {}:", vertex.getSimpleName());

            Set<DefaultEdge> outgoing = graph.outgoingEdgesOf(vertex);

            if (outgoing.isEmpty()) {
                logger.info("    -> No dependencies");
            } else {
                for (DefaultEdge edge : outgoing) {
                    Class<?> target = graph.getEdgeTarget(edge);
                    logger.info("    -> {}", target.getSimpleName());
                }
            }
        }
    }

    public SimpleDirectedGraph<Class<?>, DefaultEdge> getGraph() {
        return graph;
    }
}
