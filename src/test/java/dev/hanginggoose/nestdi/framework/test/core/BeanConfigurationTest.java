package dev.hanginggoose.nestdi.framework.test.core;

import dev.hanginggoose.nestdi.framework.annotations.Bean;
import dev.hanginggoose.nestdi.framework.annotations.Component;
import dev.hanginggoose.nestdi.framework.annotations.Configuration;
import dev.hanginggoose.nestdi.framework.core.BeanInfo;
import dev.hanginggoose.nestdi.framework.core.DIContainer;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraph;
import dev.hanginggoose.nestdi.framework.graph.DependencyGraphBuilder;
import dev.hanginggoose.nestdi.framework.scanning.ConfigurationScanner;
import org.junit.jupiter.api.Test;

import java.util.*;

import static dev.hanginggoose.nestdi.framework.core.DIContainerFactory.createCompleteBeanInfos;
import static dev.hanginggoose.nestdi.framework.core.DIContainerFactory.instantiateConfigurations;
import static org.junit.jupiter.api.Assertions.*;

public class BeanConfigurationTest {

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