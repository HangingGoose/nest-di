package dev.hanginggoose.nestdi.demo.beandemo;

import dev.hanginggoose.nestdi.framework.annotations.Bean;
import dev.hanginggoose.nestdi.framework.annotations.Configuration;

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
