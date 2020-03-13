package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangfei
 */
@Slf4j
public class RedissonLockExampleClient {

    private final RLock lock;

    private final FakeLimitedResource resource;

    private final String clientName;

    public RedissonLockExampleClient(RedissonClient client, String name, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        this.lock = client.getLock(name);
    }

    public void doWork(long waitTime, long leaseTime, TimeUnit unit) throws Exception {
        if (!lock.tryLock(waitTime, leaseTime, unit)) {
            // With the proper use of locks, this exception is unlikely to be thrown
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }
        try {
            log.info(clientName + " has the lock");
            resource.use();
        } finally {
            log.info(clientName + " releasing the lock");
            lock.unlock(); // always release the lock in a finally block
        }
    }
}
