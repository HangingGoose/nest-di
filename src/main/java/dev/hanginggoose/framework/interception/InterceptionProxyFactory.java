package dev.hanginggoose.framework.interception;

import dev.hanginggoose.framework.annotations.Logged;
import dev.hanginggoose.framework.annotations.Timed;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class InterceptionProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(InterceptionProxyFactory.class);

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        if (!requiresInterception(target.getClass())) {
            return target;
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(target.getClass());

        MethodHandler methodHandler = (self, thisMethod, proceed, args) -> {
            Method targetMethod = target.getClass()
                    .getDeclaredMethod(thisMethod.getName(), thisMethod.getParameterTypes());

            if (targetMethod.isAnnotationPresent(Logged.class)) {
                logger.info("Entering method: {}", targetMethod.getName());
            }

            long startTime = 0;
            if (targetMethod.isAnnotationPresent(Timed.class)) {
                startTime = System.currentTimeMillis();
            }

            Object result = proceed.invoke(target, args);

            if (targetMethod.isAnnotationPresent(Timed.class)) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Method {} executed in {} ms", targetMethod.getName(), duration);
            }

            if (targetMethod.isAnnotationPresent(Logged.class)) {
                logger.info("Exiting method: {}", targetMethod.getName());
            }

            return result;
        };

        try {
            return (T) factory.create(new Class<?>[0], new Object[0], methodHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy for " + target.getClass(), e);
        }
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