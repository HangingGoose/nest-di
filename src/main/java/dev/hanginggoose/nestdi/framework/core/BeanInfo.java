package dev.hanginggoose.nestdi.framework.core;

import java.lang.reflect.Method;

public class BeanInfo {
    private final String name;
    private final Class<?> beanClass;
    private final Method factoryMethod;
    private final Object configInstance;

    public BeanInfo(String name, Class<?> beanClass, Method factoryMethod, Object configInstance) {
        this.name = name;
        this.beanClass = beanClass;
        this.factoryMethod = factoryMethod;
        this.configInstance = configInstance;
    }

    public String getName() {
        return name;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public Object getConfigInstance() {
        return configInstance;
    }

    @Override
    public String toString() {
        return "BeanInfo{" +
                "name='" + name +
                "', beanClass=" + beanClass.getSimpleName() +
                '}';
    }
}