package dev.hanginggoose.framework.scanning;

import dev.hanginggoose.framework.annotations.Component;
import dev.hanginggoose.framework.annotations.Configuration;
import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.Service;
import org.reflections.Reflections;

import java.util.Set;

public class ComponentScanner {

    public Set<Class<?>> scan(String packageName) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> components = reflections.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> configurations = reflections.getTypesAnnotatedWith(Configuration.class);

        components.addAll(services);
        components.addAll(controllers);
        components.addAll(configurations);

        return components;
    }
}
