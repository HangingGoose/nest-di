package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Service;

@Service
public class DemoService {
    public String getServiceMessage() {
        return "Service is running!";
    }
}