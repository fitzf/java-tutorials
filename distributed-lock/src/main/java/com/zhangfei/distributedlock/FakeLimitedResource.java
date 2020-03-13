package com.zhangfei.distributedlock;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 模拟某些一次只能由一个进程访问的外部资源
 *
 * @author zhangfei
 */
public class FakeLimitedResource {

    private final AtomicBoolean inUse = new AtomicBoolean(false);

    private final Random random = new Random();

    public void use() throws InterruptedException {
        // in a real application this would be accessing/manipulating a shared resource

        if (!inUse.compareAndSet(false, true)) {
            throw new IllegalStateException("Needs to be used by one client at a time.");
        }

        try {
            Thread.sleep(random.nextInt(100));
        } finally {
            inUse.set(false);
        }
    }
}
