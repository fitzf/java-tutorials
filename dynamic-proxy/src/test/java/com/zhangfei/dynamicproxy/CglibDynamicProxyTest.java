package com.zhangfei.dynamicproxy;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CglibDynamicProxyTest {

    static class Dog {

        public String eat(String foodName) {
            return "Eat " + foodName;
        }
    }

    @Test
    public void testFixedValue() {
        // given
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Dog.class);
        String fixedValue = "Dog food";
        enhancer.setCallback((FixedValue) () -> fixedValue);
        Dog proxy = (Dog) enhancer.create();

        // when
        String res = proxy.eat(null);

        // then
        assertEquals(fixedValue, res);
    }

    @Test
    public void testMethodInterceptor() {
        String result = "Intercepted!";
        // 创建加强器，用来创建动态代理类
        Enhancer enhancer = new Enhancer();
        // 为代理类指定需要代理的类，也即是父类
        enhancer.setSuperclass(Dog.class);
        // 设置方法拦截器回调引用，对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现intercept() 方法进行拦截
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> result);
        Dog proxy = (Dog) enhancer.create();

        assertEquals(result, proxy.eat(null));
    }

    @Test
    public void testMethodInterceptorInvoke() {
        // 创建加强器，用来创建动态代理类
        Enhancer enhancer = new Enhancer();
        // 为代理类指定需要代理的类，也即是父类
        enhancer.setSuperclass(Dog.class);
        // 设置方法拦截器回调引用，对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现intercept() 方法进行拦截
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            log.info("before...");
            Object result = proxy.invokeSuper(obj, args);
            // Object result = method.invoke(target, args);
            log.info("after.");
            return result;
        });
        Dog proxy = (Dog) enhancer.create();
        String foodName = "Dog food.";
        assertEquals("Eat " + foodName, proxy.eat(foodName));
    }
}
