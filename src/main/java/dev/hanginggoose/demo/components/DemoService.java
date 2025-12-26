package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Service;

@Service
public class DemoService {
    DemoRepository repository;

    public DemoService(DemoRepository repository) {
        this.repository = repository;
    }

    public String process() {
        return "Processed data: " + repository.getData();
    }
}