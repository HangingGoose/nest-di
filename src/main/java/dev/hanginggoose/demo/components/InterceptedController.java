package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Controller;
import dev.hanginggoose.framework.annotations.Logged;
import dev.hanginggoose.framework.annotations.Timed;

@Controller
public class InterceptedController {

    @Logged
    @Timed
    public String interceptedMethod() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        return "Processed with interception";
    }
}