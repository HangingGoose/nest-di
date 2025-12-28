package dev.hanginggoose.demo.beandemo;

import dev.hanginggoose.framework.annotations.Bean;
import dev.hanginggoose.framework.annotations.Configuration;

@Configuration
public class AppConfig {

    @Bean(name = "customGreeting")
    public String greetingMessage() {
        return "Hello, Dependency Injection!";
    }

    @Bean
    public Integer answerToEverything() {
        return 42;
    }
}
