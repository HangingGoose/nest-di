package dev.hanginggoose.framework.test.core;

import dev.hanginggoose.framework.annotations.Bean;
import dev.hanginggoose.framework.annotations.Component;
import dev.hanginggoose.framework.annotations.Configuration;
import dev.hanginggoose.framework.core.BeanInfo;
import dev.hanginggoose.framework.core.DIContainer;
import dev.hanginggoose.framework.core.DIContainerFactory;
import dev.hanginggoose.framework.graph.DependencyGraph;
import dev.hanginggoose.framework.graph.DependencyGraphBuilder;
import dev.hanginggoose.framework.scanning.ConfigurationScanner;
import org.junit.jupiter.api.Test;

import java.util.*;

import static dev.hanginggoose.framework.core.DIContainerFactory.createCompleteBeanInfos;
import static dev.hanginggoose.framework.core.DIContainerFactory.instantiateConfigurations;
import static org.junit.jupiter.api.Assertions.*;

public class BeanConfigurationTest {

    @Test
    public void testBeanCreation() {
        DIContainer container = DIContainerFactory.create("dev.hanginggoose.demo");
        container.start();

        String greeting = (String) container.getBean("customGreeting");
        assertNotNull(greeting);
        assertEquals("Hello, Dependency Injection!", greeting);

        Integer magicNumber = container.getBean(Integer.class);
        assertNotNull(magicNumber);
        assertEquals(42, magicNumber);
    }

    @Configuration
    public static class MixedConfig {
        @Bean
        public String configBean() {
            return "From config";
        }
    }

    @Component
    public static class MixedComponent {
        private final String value;

        public MixedComponent(String value) {
            this.value = value;
        }

        public String getValue() {
            return "Component: " + value;
        }
    }

    @Test
    public void testBeanAndComponentMix() {
        Set<Class<?>> components = new HashSet<>();
        components.add(MixedConfig.class);
        components.add(MixedComponent.class);

        ConfigurationScanner configScanner = new ConfigurationScanner();
        List<BeanInfo> configBeanInfos = configScanner.scanBeanMethods(MixedConfig.class);
        for (BeanInfo beanInfo : configBeanInfos) {
            components.add(beanInfo.getBeanClass());
        }

        Map<Class<?>, Object> configInstances = new HashMap<>();
        Object configInstance = instantiateConfigurations(Set.of(MixedConfig.class)).get(MixedConfig.class);
        configInstances.put(MixedConfig.class, configInstance);

        List<BeanInfo> completeBeanInfos = createCompleteBeanInfos(
                Map.of(MixedConfig.class, configBeanInfos),
                configInstances
        );

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        DependencyGraph dependencyGraph = builder.build(components, completeBeanInfos);

        DIContainer mixedContainer = new DIContainer(dependencyGraph, completeBeanInfos);

        mixedContainer.start();

        MixedComponent component = mixedContainer.getBean(MixedComponent.class);
        assertNotNull(component);
        assertEquals("Component: From config", component.getValue());
    }
}