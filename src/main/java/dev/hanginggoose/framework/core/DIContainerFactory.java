package dev.hanginggoose.framework.core;

import dev.hanginggoose.framework.graph.DependencyGraphBuilder;
import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DIContainerFactory {
    private static final Logger logger = LoggerFactory.getLogger(DIContainerFactory.class);

    public static DIContainer create(String basePackage) {
        logger.info("Creating DI Container for package: {}", basePackage);

        ComponentScanner scanner = new ComponentScanner();
        var components = scanner.scan(basePackage);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(components);

        return new DIContainer(dependencyGraph);
    }

    public static DIContainer create(String[] basePackages) {
        logger.info("Creating DI Container for packages: {}", Arrays.toString(basePackages));

        ComponentScanner scanner = new ComponentScanner();
        var components = scanner.scanPackages(basePackages);

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var dependencyGraph = builder.build(components);

        return new DIContainer(dependencyGraph);
    }
}
