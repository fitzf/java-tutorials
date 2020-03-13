package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangfei
 */
@Slf4j
public class CuratorLockExampleClient {

    private final InterProcessMutex lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public CuratorLockExampleClient(CuratorFramework client, String lockPath, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        this.lock = new InterProcessMutex(client, lockPath);
    }

    public void doWork(long time, TimeUnit unit) throws Exception {
        if (!lock.acquire(time, unit)) {
            // With the proper use of locks, this exception is unlikely to be thrown
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }
        try {
            log.info(clientName + " has the lock");
            resource.use();
        } finally {
            log.info(clientName + " releasing the lock again");
            lock.release(); // always release the lock in a finally block
        }
    }
}
