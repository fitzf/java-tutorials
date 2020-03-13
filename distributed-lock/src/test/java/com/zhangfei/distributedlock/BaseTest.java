package com.zhangfei.distributedlock;

public class BaseTest {

    // 并发数 / 客户端数
    protected static final int QTY = 5;

    // 请求次数
    protected static final int REPETITIONS = 10 * 10;

    // FakeLimitedResource simulates some external resource that can only be access by one process at a time
    protected static final FakeLimitedResource resource = new FakeLimitedResource();
}
