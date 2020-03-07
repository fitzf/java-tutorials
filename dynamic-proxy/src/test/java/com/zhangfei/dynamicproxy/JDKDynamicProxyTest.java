package com.zhangfei.dynamicproxy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class JDKDynamicProxyTest {

    interface Animal {

        String eat(String foodName);
    }

    static class Cat implements Animal {

        @Override
        public String eat(String foodName) {
            return "After eating " + foodName + ", I'm full.";
        }
    }

    static class AnimalInvocationHandler implements InvocationHandler {

        private final Animal target;

        public AnimalInvocationHandler(Animal target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            preEat();
            Object result = method.invoke(target, args);
            afterEat();
            return result;
        }

        private void preEat() {
            log.info("Clean food containers before eating.");
        }

        private void afterEat() {
            log.info("Feed water after eating.");
        }
    }

    @Test
    public void testCreateProxyObjectAndInvoke() {
        Animal animal = new Cat();
        Animal proxy = (Animal) Proxy.newProxyInstance(animal.getClass().getClassLoader(),
                animal.getClass().getInterfaces(), new AnimalInvocationHandler(animal));
        assertEquals("After eating Cat food, I'm full.", proxy.eat("Cat food"));
        assertTrue(Proxy.isProxyClass(proxy.getClass()));
    }
}
