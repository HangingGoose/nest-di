package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Component;

@Component
public class DemoRepository {
    public DemoRepository() {
    }

    public String getData() {
        return "Data from repository";
    }
}