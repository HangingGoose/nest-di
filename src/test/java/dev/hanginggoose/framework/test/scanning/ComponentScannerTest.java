package dev.hanginggoose.framework.test.scanning;

import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ComponentScannerTest {

    private ComponentScanner scanner;

    @BeforeEach
    public void setup() {
        scanner = new ComponentScanner();
    }

    @Test
    public void testComponentScannerFindsComponentsInPackage() {
        Set<Class<?>> components = scanner.scan("dev.hanginggoose.demo");

        assertFalse(components.isEmpty());

        boolean foundDemoRepository = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("InMemoryBrandRepository"));
        boolean foundDemoService = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("BrandService"));
        boolean foundDemoController = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("BrandController"));


        assertTrue(foundDemoRepository);
        assertTrue(foundDemoService);
        assertTrue(foundDemoController);
    }

    @Test
    public void testComponentScannerDoesNotFindComponentsOutsidePackage() {
        Set<Class<?>> components = scanner.scan("dev.hanginggoose.demo.domain");

        boolean foundDemoRepository = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("InMemoryBrandRepository"));
        boolean foundDemoService = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("BrandService"));
        boolean foundDemoController = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("BrandController"));


        assertFalse(foundDemoRepository);
        assertFalse(foundDemoService);
        assertFalse(foundDemoController);
    }

    @Test
    public void testComponentScannerFindsComponentsInMultiplePackages() {
        String[] packages = {
                "dev.hanginggoose.demo.infrastructure.brand",
                "dev.hanginggoose.demo.infrastructure.product"
        };
        Set<Class<?>> components = scanner.scanPackages(packages);

        assertFalse(components.isEmpty());
        assertTrue(components.stream().anyMatch(clazz ->
                clazz.getSimpleName().equals("InMemoryBrandRepository")));
        assertTrue(components.stream().anyMatch(clazz ->
                clazz.getSimpleName().equals("InMemoryProductRepository")));
    }

    @Test
    public void testComponentScannerDoesNotFindComponentsOutsideMultiplePackages() {
        String[] packages = {
                "dev.hanginggoose.demo.domain",
                "dev.hanginggoose.demo.beandemo"
        };
        Set<Class<?>> components = scanner.scanPackages(packages);

        assertFalse(components.stream().anyMatch(clazz ->
                clazz.getSimpleName().equals("InMemoryBrandRepository")));
        assertFalse(components.stream().anyMatch(clazz ->
                clazz.getSimpleName().equals("InMemoryProductRepository")));
    }
}