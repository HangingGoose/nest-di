package dev.hanginggoose.framework.graph;

import dev.hanginggoose.framework.annotations.Autowired;
import dev.hanginggoose.framework.core.BeanInfo;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

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
            analyzeParameters(componentClass, componentClass.getSimpleName(), parameters, allComponents);
        }
    }

    public void analyzeBeanDependencies(BeanInfo beanInfo, Set<Class<?>> allComponents) {
        Method beanMethod = beanInfo.getFactoryMethod();
        if (beanMethod != null) {
            Parameter[] parameters = beanMethod.getParameters();
            analyzeParameters(beanInfo.getBeanClass(), beanMethod.getName(), parameters, allComponents);
        }
    }

    private void analyzeParameters(Class<?> className, String source, Parameter[] parameters, Set<Class<?>> allComponents) {
        for (Parameter parameter : parameters) {
            Class<?> parameterType = parameter.getType();

            Optional<Class<?>> dependency = findComponentByType(parameterType, allComponents);

            if (dependency.isPresent()) {
                addDependency(dependency.get(), className);
            } else {
                logger.warn("No component found for dependency type: {} in bean method {}",
                        parameterType.getSimpleName(),
                        source);
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

    public List<Class<?>> getTopologicalOrder() {
        if (hasCycles()) {
            throw new IllegalStateException("Cannot get topological order: graph contains cycles");
        }

        Map<Class<?>, Integer> inDegree = new HashMap<>();
        List<Class<?>> result = new ArrayList<>();

        for (Class<?> vertex : graph.vertexSet()) {
            inDegree.put(vertex, graph.inDegreeOf(vertex));
        }

        Queue<Class<?>> queue = new LinkedList<>();
        for (Class<?> vertex : graph.vertexSet()) {
            if (inDegree.get(vertex) == 0) {
                queue.add(vertex);
            }
        }

        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            result.add(current);

            for (DefaultEdge edge : graph.outgoingEdgesOf(current)) {
                Class<?> neighbor = graph.getEdgeTarget(edge);
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (result.size() != graph.vertexSet().size()) {
            throw new IllegalStateException("Unexpected error: topological sort incomplete (possible undetected cycle)");
        }

        return result;
    }

    public boolean hasCycles() {
        return cycleDetector.detectCycles();
    }

    public Constructor<?> findConstructor(Constructor<?>[] constructors) {
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

    public Optional<Class<?>> findComponentByType(Class<?> type, Set<Class<?>> allComponents) {
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