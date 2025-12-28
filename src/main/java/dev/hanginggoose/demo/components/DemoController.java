package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.InputMapping;

@Controller
public class DemoController {
    DemoService service;

    public DemoController(DemoService service) {
        this.service = service;
    }

    @InputMapping("process")
    public String handleRequest() {
        return "Controller: " + service.process();
    }

    @InputMapping("greet")
    public String greet() {
        return "Hello from controller!";
    }
}