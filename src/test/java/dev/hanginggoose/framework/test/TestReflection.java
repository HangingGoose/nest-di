package dev.hanginggoose.framework.test;

import dev.hanginggoose.framework.annotations.Component;
import dev.hanginggoose.framework.scanning.ComponentScanner;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestReflection {

    @Test
    public void componentAnnotationDetected() {
        ComponentScanner scanner = new ComponentScanner();

        Set<Class<?>> components = scanner.scan("dev.hanginggoose.framework.test");

        assertFalse(components.isEmpty());

        boolean found = components.stream()
                .anyMatch(clazz -> clazz.getSimpleName().equals("TestComponent"));

        assertTrue(found);
    }

    @Component
    public static class TestComponent {
        //Enkel voor detectie
    }
}