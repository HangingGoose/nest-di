package dev.hanginggoose.framework.test;

import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleScannerTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleScannerTest.class);

    @Test
    public void testComponentScannerFindsComponents() {
        ComponentScanner scanner = new ComponentScanner();
        Set<Class<?>> components = scanner.scan("dev.hanginggoose.demo");

        assertFalse(components.isEmpty());

        log.info("Found components:");
        components.forEach(clazz -> log.info("  - {} ({})", clazz.getSimpleName(), clazz.getName()));

        boolean foundDemoComponent = components.stream().anyMatch(clazz -> clazz.getSimpleName().equals("DemoComponent"));

        assertTrue(foundDemoComponent);
    }
}