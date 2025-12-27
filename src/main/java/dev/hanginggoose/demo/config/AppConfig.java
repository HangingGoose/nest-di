package dev.hanginggoose.demo.config;

import dev.hanginggoose.demo.components.DemoRepository;
import dev.hanginggoose.demo.components.DemoService;
import dev.hanginggoose.framework.annotations.Bean;
import dev.hanginggoose.framework.annotations.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public DemoRepository demoRepository() {
        return new DemoRepository();
    }

    @Bean
    public DemoService demoService(DemoRepository repository) {
        return new DemoService(repository);
    }

    @Bean(name = "customGreeting")
    public String greetingMessage() {
        return "Hello, Dependency Injection!";
    }

    @Bean
    public Integer answerToEverything() {
        return 42;
    }
}
