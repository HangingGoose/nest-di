# Nest DI – README
> Lightweight, annotation-based Dependency-Injection framework for Java – made as a project for **Programming 3 (2025-2026)** at **Karel de Grote-Hogeschool**.  
> Inspired by Spring Framework
---

## Author
**Name:** HangingGoose  
**Project:** Nest DI (v1.0.0)

---

## What does it do?
Nest DI supports:
- component scanning
- constructor injection with optional `@Autowired`
- detection of circular dependencies
- beans defined in `@Configuration`-classes
- simple controller commands in console
- interception (`@Logged`, `@Timed`)

---

## Usage in 3 steps

### 1. Mark components with one of the following Component annotations:
- `@Component`
- `@Controller`
- `@Repository`
- `@Service`

```java
import dev.hanginggoose.nestdi.framework.annotations.Service;

@Service
public class HelloService {
    public String greet(String name) {
        return "Hello " + name;
    }
}
```

### 2. Provide bean factories in `@Configuration`-annotated classes:
```java
import dev.hanginggoose.nestdi.framework.annotations.Bean;
import dev.hanginggoose.nestdi.framework.annotations.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public String appName() {
        return "Nest DI Demo";
    }
}
```

### 3. Create controller with console commands
```java
import dev.hanginggoose.nestdi.framework.annotations.Autowired;
import dev.hanginggoose.nestdi.framework.annotations.Controller;
import dev.hanginggoose.nestdi.framework.annotations.InputMapping;

@Controller
public class HelloController {

    private final HelloService service;
    private final String appName;

    // @Autowired is optional if only one constructor exists
    @Autowired
    public HelloController(HelloService service, String appName) {
        this.service = service;
        this.appName = appName;
    }

    @InputMapping("greet")
    public String greet(String name) {
        return service.greet(name) + " from " + appName;
    }
}
```

---

## Commands

| Task          | Command           |
|---------------|-------------------|
| **Compile**   | `./gradlew build` |
| **Run demo**  | `./gradlew run`   |
| **Run tests** | `./gradlew test`  |

> Requirements: Java 21+ and Gradle 8+

---