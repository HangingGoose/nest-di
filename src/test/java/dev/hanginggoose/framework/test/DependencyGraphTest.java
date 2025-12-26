package dev.hanginggoose.framework.test;

import dev.hanginggoose.framework.annotations.Component;
import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.Service;
import dev.hanginggoose.framework.graph.DependencyGraph;
import dev.hanginggoose.framework.graph.DependencyGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyGraphTest {

    @Test
    public void testDependencyGraphDetectsCyclicDependencies() {
        @Service
        class ServiceA {
        }
        @Service
        class ServiceB {
        }

        DependencyGraph dependencyGraph = new DependencyGraph();

        dependencyGraph.addComponent(ServiceA.class);
        dependencyGraph.addComponent(ServiceB.class);

        dependencyGraph.getGraph().addEdge(ServiceA.class, ServiceB.class);
        assertFalse(dependencyGraph.hasCycles());

        dependencyGraph.getGraph().addEdge(ServiceB.class, ServiceA.class);
        assertTrue(dependencyGraph.hasCycles());
    }

    @Component
    static class TestRepository {
        public TestRepository() {
        }
    }

    @Service
    static class TestService {
        public TestService(TestRepository repository) {
        }
    }

    @Controller
    static class TestController {
        public TestController(TestService service) {
        }
    }

    @Test
    public void testDependencyGraphBuilderWithRealComponents() {
        Set<Class<?>> components = new HashSet<>();
        components.add(TestRepository.class);
        components.add(TestService.class);
        components.add(TestController.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph graph = builder.build(components);

        assertFalse(graph.hasCycles());
    }

    @Component
    static class CycleRepository {
        public CycleRepository() {
        }
    }

    @Service
    static class CycleService {
        public CycleService(OtherCycleService otherCycleService, CycleRepository repository) {
        }
    }

    @Service
    static class OtherCycleService {
        public OtherCycleService(CycleService cycleService) {
        }
    }

    @Component
    static class CycleController {
        public CycleController(CycleService service) {
        }
    }

    @Test
    public void testDependencyGraphBuilderWithRealComponentsWithCycles() {
        Set<Class<?>> components = new HashSet<>();
        components.add(CycleRepository.class);
        components.add(CycleService.class);
        components.add(OtherCycleService.class);
        components.add(CycleController.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph graph = builder.build(components);

        assertTrue(graph.hasCycles());
    }

    @Test
    public void testDependencyGraphBuilderTopologicalOrder() {
        Set<Class<?>> components = new HashSet<>();
        components.add(TestRepository.class);
        components.add(TestService.class);
        components.add(TestController.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph graph = builder.build(components);

        assertFalse(graph.hasCycles());

        var order = graph.getTopologicalOrder();

        assertEquals(3, order.size());
        assertEquals(TestRepository.class, order.get(0));
        assertEquals(TestService.class, order.get(1));
        assertEquals(TestController.class, order.get(2));
    }
}