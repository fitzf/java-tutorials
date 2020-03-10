package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangfei
 */
@Slf4j
public class ExampleClientThatLock {

    private final InterProcessMutex lock;
    private final FakeLimitedResource resource;
    private final String clientName;

    public ExampleClientThatLock(CuratorFramework client, String lockPath, FakeLimitedResource resource, String clientName) {
        this.resource = resource;
        this.clientName = clientName;
        this.lock = new InterProcessMutex(client, lockPath);
    }

    public void doWork(long time, TimeUnit unit) throws Exception {
        if (!lock.acquire(time, unit)) {
            throw new IllegalStateException(clientName + " could not acquire the lock");
        }
        try {
            log.info(clientName + " has the lock");
            resource.use();
            if (!lock.acquire(time, unit)) {
                throw new IllegalStateException(clientName + " could not acquire the lock again");
            }
            log.info(clientName + " acquire the lock again");
            log.info(clientName + " releasing the lock");
            lock.release();
        } finally {
            log.info(clientName + " releasing the lock again");
            lock.release(); // always release the lock in a finally block
        }
    }
}
