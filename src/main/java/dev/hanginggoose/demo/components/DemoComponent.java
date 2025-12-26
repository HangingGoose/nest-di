package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Component;

@Component
public class DemoComponent {

    private final String message;

    public DemoComponent() {
        this.message = "Hello from DemoComponent!";
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DemoComponent{message='" + message + "'}";
    }
}