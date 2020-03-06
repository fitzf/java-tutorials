package com.zhangfei.dynamicproxy.cglib;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangfei
 */
@Slf4j
public class Dog {

    public String call() {
        log.info("wang wang wang");
        return "Dog ..";
    }
}
