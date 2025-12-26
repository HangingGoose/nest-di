package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Controller;

@Controller
public class DemoController {
    DemoService service;

    public DemoController(DemoService service) {
        this.service = service;
    }

    public String handleRequest() {
        return "Controller: " + service.process();
    }
}
