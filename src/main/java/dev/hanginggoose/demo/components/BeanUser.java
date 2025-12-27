package dev.hanginggoose.demo.components;

import dev.hanginggoose.framework.annotations.Component;

@Component
public class BeanUser {
    private final String greeting;
    private final Integer answer;

    public BeanUser(String greeting, Integer answer) {
        this.greeting = greeting;
        this.answer = answer;
    }

    public String useBeans() {
        return greeting + " The answer is " + answer;
    }
}
