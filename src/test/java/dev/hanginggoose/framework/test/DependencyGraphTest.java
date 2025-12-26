package dev.hanginggoose.framework.test;

import dev.hanginggoose.framework.annotations.Service;
import dev.hanginggoose.framework.graph.DependencyGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DependencyGraphTest {

    @Service
    public static class ServiceA {
    }

    @Service
    public static class ServiceB {
    }

    @Test
    public void testDependencyGraph() {
        DependencyGraph dependencyGraph = new DependencyGraph();

        dependencyGraph.addComponent(ServiceA.class);
        dependencyGraph.addComponent(ServiceB.class);

        dependencyGraph.getGraph().addEdge(ServiceA.class, ServiceB.class);
        assertFalse(dependencyGraph.hasCycles());

        dependencyGraph.getGraph().addEdge(ServiceB.class, ServiceA.class);
        assertTrue(dependencyGraph.hasCycles());
    }
}