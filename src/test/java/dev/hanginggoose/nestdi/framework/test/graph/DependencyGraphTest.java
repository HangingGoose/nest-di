package dev.hanginggoose.nestdi.framework.test.graph;

import dev.hanginggoose.nestdi.framework.annotations.Component;
import dev.hanginggoose.nestdi.framework.annotations.Controller;
import dev.hanginggoose.nestdi.framework.annotations.Repository;
import dev.hanginggoose.nestdi.framework.annotations.Service;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraph;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    @Repository
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
        DependencyGraph dependencyGraph = builder.build(components, new ArrayList<>());

        assertFalse(dependencyGraph.hasCycles());
    }

    @Repository
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
        DependencyGraph dependencyGraph = builder.build(components, new ArrayList<>());

        assertTrue(dependencyGraph.hasCycles());
    }

    @Test
    public void testDependencyGraphBuilderTopologicalOrder() {
        Set<Class<?>> components = new HashSet<>();
        components.add(TestRepository.class);
        components.add(TestService.class);
        components.add(TestController.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph dependencyGraph = builder.build(components, new ArrayList<>());

        assertFalse(dependencyGraph.hasCycles());

        var order = dependencyGraph.getTopologicalOrder();

        assertEquals(3, order.size());
        assertEquals(TestRepository.class, order.get(0));
        assertEquals(TestService.class, order.get(1));
        assertEquals(TestController.class, order.get(2));
    }
}