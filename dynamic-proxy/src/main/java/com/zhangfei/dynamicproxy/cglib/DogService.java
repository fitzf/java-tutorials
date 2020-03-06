package com.zhangfei.dynamicproxy.cglib;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangfei
 */
@Slf4j
public class DogService {

    public String eat(String foodName) {
        return "Eat " + foodName;
    }
}
