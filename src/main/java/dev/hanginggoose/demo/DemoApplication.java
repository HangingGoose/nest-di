package dev.hanginggoose.demo;

import dev.hanginggoose.framework.graph.DependencyGraphBuilder;
import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {
    private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        logger.info("Starting DI Container Demo");

        ComponentScanner scanner = new ComponentScanner();
        var components = scanner.scan("dev.hanginggoose.demo");

        DependencyGraphBuilder builder = new DependencyGraphBuilder();
        var graph = builder.build(components);

        logger.info("Dependency graph constructed successfully");
    }
}
