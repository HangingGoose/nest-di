package dev.hanginggoose.framework.interception;

import dev.hanginggoose.framework.annotations.Logged;
import dev.hanginggoose.framework.annotations.Timed;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class InterceptionProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(InterceptionProxyFactory.class);

    public static <T> T createProxy(T target) {
        if (!requiresInterception(target.getClass())) {
            return target;
        }

        if (hasInterface(target.getClass())) {
            logger.debug("Creating Java Proxy for {}",
                    target.getClass().getSimpleName());
            return createJavaProxy(target);
        } else {
            logger.debug("Creating Javassist Proxy for {}",
                    target.getClass().getSimpleName());
            return createJavassistProxy(target);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createJavaProxy(T target) {
        Class<?> targetClass = target.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();

        if (interfaces.length == 0) {
            throw new IllegalStateException("Cannot create Java Proxy for class without interfaces: "
                    + targetClass.getName());
        }

        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                (proxy, method, arguments) -> method.invoke(target, arguments)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T createJavassistProxy(T target) {
        Class<?> targetClass = target.getClass();

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(targetClass);
        factory.setFilter(
                method ->
                        Arrays.stream(method.getDeclaredAnnotations()).anyMatch(
                                annotation -> annotation.annotationType().equals(Logged.class) ||
                                        annotation.annotationType().equals(Timed.class))
        );

        MethodHandler handler = (self, method, proceed, arguments) -> {

            if (method.isAnnotationPresent(Logged.class)) {
                logger.info("Entering method: {}", method.getName());
            }

            long startTime = 0;
            if (method.isAnnotationPresent(Timed.class)) {
                startTime = System.currentTimeMillis();
            }

            Object result = method.invoke(target, arguments);

            if (method.isAnnotationPresent(Timed.class)) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Method {} executed in {} ms", method.getName(), duration);
            }

            if (method.isAnnotationPresent(Logged.class)) {
                logger.info("Exiting method: {}", method.getName());
            }

            return result;
        };

        try {
            return (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for " + target.getClass(), e);
        }
    }

    private static boolean hasInterface(Class<?> targetClass) {
        return targetClass.getInterfaces().length > 0;
    }

    private static boolean requiresInterception(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Logged.class) ||
                    method.isAnnotationPresent(Timed.class)) {
                return true;
            }
        }
        return false;
    }
}