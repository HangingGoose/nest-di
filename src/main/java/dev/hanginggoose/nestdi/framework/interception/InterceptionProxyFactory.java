package dev.hanginggoose.nestdi.framework.interception;

import dev.hanginggoose.nestdi.framework.annotations.interception.Logged;
import dev.hanginggoose.nestdi.framework.annotations.interception.Timed;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
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
                (proxy, method, arguments) -> {
                    Method targetMethod = findTargetMethod(targetClass, method);
                    if (targetMethod == null) {
                        return method.invoke(target, arguments);
                    }

                    return invokeMethod(target, method, arguments, targetMethod);
                }
        );
    }

    private static Method findTargetMethod(Class<?> targetClass, Method interfaceMethod) {
        try {
            return targetClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
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

        MethodHandler handler = (self, method, proceed, arguments) ->
                invokeMethod(target, method, arguments, method);

        try {
            return (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for " + target.getClass(), e);
        }
    }

    private static <T> Object invokeMethod(T target, Method method, Object[] arguments, Method targetMethod) throws IllegalAccessException, InvocationTargetException {
        if (targetMethod.isAnnotationPresent(Logged.class)) {
            logger.info("Entering method: {}", targetMethod.getName());
        }

        long startTime = 0;
        if (targetMethod.isAnnotationPresent(Timed.class)) {
            startTime = System.currentTimeMillis();
        }

        Object result = method.invoke(target, arguments);

        if (targetMethod.isAnnotationPresent(Timed.class)) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Method {} executed in {} ms", targetMethod.getName(), duration);
        }

        if (targetMethod.isAnnotationPresent(Logged.class)) {
            logger.info("Exiting method: {}", targetMethod.getName());
        }

        return result;
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