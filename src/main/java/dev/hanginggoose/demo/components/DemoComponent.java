package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Component;

@Component
public class DemoComponent {
    public String hello() {
        return "Hello from DemoComponent!";
    }
}