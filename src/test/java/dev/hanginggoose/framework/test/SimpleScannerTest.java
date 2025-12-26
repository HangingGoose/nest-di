package dev.hanginggoose.framework.test;

import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleScannerTest {

    private ComponentScanner scanner;

    @BeforeEach
    public void setup() {
        scanner = new ComponentScanner();
    }

    @Test
    public void testComponentScannerFindsComponents() {
        Set<Class<?>> components = scanner.scan("dev.hanginggoose.demo");

        assertFalse(components.isEmpty());

        boolean foundDemoComponent = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("DemoComponent"));
        boolean foundDemoService = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("DemoService"));

        assertTrue(foundDemoComponent);
        assertTrue(foundDemoService);
    }

    @Test
    public void testScanMultiplePackages() {
        String[] packages = {"dev.hanginggoose.demo", "dev.hanginggoose.framework"};
        Set<Class<?>> components = scanner.scanPackages(packages);

        assertFalse(components.isEmpty());
        boolean foundDemoComponent = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("DemoComponent"));
        assertTrue(foundDemoComponent);
    }
}