package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Repository;

@Repository
public class DemoRepository {
    public DemoRepository() {
    }

    public String getData() {
        return "Data from repository";
    }
}