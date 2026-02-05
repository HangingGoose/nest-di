package dev.hanginggoose.nestdi.framework.test.interception;

import dev.hanginggoose.nestdi.framework.annotations.interception.Logged;
import dev.hanginggoose.nestdi.framework.annotations.interception.Timed;
import dev.hanginggoose.nestdi.framework.interception.InterceptionProxyFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class InterceptionProxyFactoryTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    public static class NoInterceptionService {
        public String sayHello() {
            return "Hello";
        }
    }

    public static class LoggedService {
        @Logged
        public String sayHello() {
            return "Hello from LoggedService";
        }
    }

    public static class TimedService {
        @Timed
        public void slowMethod() throws InterruptedException {
            Thread.sleep(50);
        }
    }

    public static class CombinedService {
        @Logged
        @Timed
        public String combined() {
            return "Combined";
        }
    }

    @Test
    public void testNoInterceptionWhenNoAnnotations() {
        NoInterceptionService target = new NoInterceptionService();
        NoInterceptionService proxy = InterceptionProxyFactory.createProxy(target);

        assertSame(target, proxy);
    }

    @Test
    public void testLoggedInterception() {
        LoggedService target = new LoggedService();
        LoggedService proxy = InterceptionProxyFactory.createProxy(target);

        assertNotSame(target, proxy);

        String result = proxy.sayHello();
        assertEquals("Hello from LoggedService", result);

        String output = outputStream.toString();
        assertTrue(output.contains("Entering method: sayHello"));
        assertTrue(output.contains("Exiting method: sayHello"));
    }

    @Test
    public void testTimedInterception() throws Exception {
        TimedService target = new TimedService();
        TimedService proxy = InterceptionProxyFactory.createProxy(target);

        assertNotSame(target, proxy);

        proxy.slowMethod();

        String output = outputStream.toString();
        assertTrue(output.contains("Method slowMethod executed in"));
        assertTrue(output.contains("ms"));
    }

    @Test
    public void testCombinedLoggedAndTimed() {
        CombinedService target = new CombinedService();
        CombinedService proxy = InterceptionProxyFactory.createProxy(target);

        assertNotSame(target, proxy);

        String result = proxy.combined();
        assertEquals("Combined", result);

        String output = outputStream.toString();
        assertTrue(output.contains("Entering method: combined"));
        assertTrue(output.contains("Method combined executed in"));
        assertTrue(output.contains("Exiting method: combined"));
    }

    public interface GreetingService {
        String greet(String name);
    }

    public static class GreetingServiceImpl implements GreetingService {
        @Logged
        @Override
        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    public interface SlowService {
        void work();
    }

    public static class SlowServiceImpl implements SlowService {
        @Timed
        @Override
        public void work() {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    public void testInterceptionOnInterfaceWithLogged() {
        GreetingServiceImpl target = new GreetingServiceImpl();
        GreetingService proxy = InterceptionProxyFactory.createProxy(target);

        assertNotSame(target, proxy);

        String result = proxy.greet("KDG");
        assertEquals("Hello, KDG", result);

        String output = outputStream.toString();
        assertTrue(output.contains("Entering method: greet"));
        assertTrue(output.contains("Exiting method: greet"));
    }

    @Test
    public void testInterceptionOnInterfaceWithTimed() {
        SlowServiceImpl target = new SlowServiceImpl();
        SlowService proxy = InterceptionProxyFactory.createProxy(target);

        assertNotSame(target, proxy);

        proxy.work();

        String output = outputStream.toString();
        assertTrue(output.contains("Method work executed in"));
        assertTrue(output.contains("ms"));
    }

    @Test
    public void testNoInterceptionOnInterfaceWhenNoAnnotations() {
        interface SimpleService {
            String get();
        }

        class SimpleServiceImpl implements SimpleService {
            @Override
            public String get() {
                return "simple";
            }
        }

        SimpleServiceImpl target = new SimpleServiceImpl();
        SimpleService proxy = InterceptionProxyFactory.createProxy(target);

        assertSame(target, proxy);
    }
}