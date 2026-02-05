package dev.hanginggoose.nestdi.framework.test.core;

import dev.hanginggoose.nestdi.framework.annotations.Autowired;
import dev.hanginggoose.nestdi.framework.annotations.components.Component;
import dev.hanginggoose.nestdi.framework.core.DIContainer;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraph;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraphBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DiContainerTest {

    @Component
    public static class TestComponent {
        public String getMessage() {
            return "Hello from TestComponent";
        }
    }

    @Component
    public static class DependentComponent {
        private final TestComponent testComponent;

        public DependentComponent(TestComponent testComponent) {
            this.testComponent = testComponent;
        }

        public String getCombinedMessage() {
            return "Dependent: " + testComponent.getMessage();
        }
    }

    @Component
    public static class MoreDependentComponent {
        private final DependentComponent dependentComponent;

        public MoreDependentComponent(DependentComponent dependentComponent) {
            this.dependentComponent = dependentComponent;
        }

        public String getFullMessage() {
            return "MoreDependent: " + dependentComponent.getCombinedMessage();
        }
    }

    private DIContainer container;

    @BeforeEach
    public void setup() {
        Set<Class<?>> components = new HashSet<>();
        components.add(TestComponent.class);
        components.add(DependentComponent.class);
        components.add(MoreDependentComponent.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph dependencyGraph = builder.build(components, new ArrayList<>());

        container = new DIContainer(dependencyGraph, null);
    }

    @Test
    public void testContainerStart() {
        assertDoesNotThrow(() -> container.start());
        assertEquals(3, container.getAllBeans().size());
    }

    @Test
    public void testGetBean() {
        container.start();

        TestComponent testComponent = container.getBean(TestComponent.class);
        assertNotNull(testComponent);
        assertEquals("Hello from TestComponent", testComponent.getMessage());

        DependentComponent dependentComponent = container.getBean(DependentComponent.class);
        assertNotNull(dependentComponent);
        assertEquals("Dependent: Hello from TestComponent", dependentComponent.getCombinedMessage());

        MoreDependentComponent moreDependentComponent = container.getBean(MoreDependentComponent.class);
        assertNotNull(moreDependentComponent);
        assertEquals("MoreDependent: Dependent: Hello from TestComponent", moreDependentComponent.getFullMessage());

        TestComponent testComponent2 = container.getBean(TestComponent.class);
        assertSame(testComponent, testComponent2);
    }

    @Test
    public void testDependencyInjection() {
        container.start();

        MoreDependentComponent moreDependentComponent = container.getBean(MoreDependentComponent.class);
        assertNotNull(moreDependentComponent);
        assertEquals("MoreDependent: Dependent: Hello from TestComponent", moreDependentComponent.getFullMessage());
    }

    @Test
    public void testNonBeanRequest() {
        class NonBeanComponent {
        }
        container.start();
        assertThrows(IllegalStateException.class, () -> container.getBean(NonBeanComponent.class));
    }

    @Component
    static class OptionalComponent {
        private final String optional;

        @Autowired(required = false)
        public OptionalComponent(String optional) {
            this.optional = optional;
        }

        public String getValue() {
            return optional == null ? "default" : optional;
        }
    }

    @Test
    public void testOptionalAutowiredWhenNoAvailableBean() {
        Set<Class<?>> components = Set.of(OptionalComponent.class);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph graph = builder.build(components, new ArrayList<>());

        DIContainer testContainer = new DIContainer(graph, null);
        testContainer.start();

        OptionalComponent optionalComponent = testContainer.getBean(OptionalComponent.class);
        assertNotNull(optionalComponent);
        assertEquals("default", optionalComponent.getValue());
    }
}