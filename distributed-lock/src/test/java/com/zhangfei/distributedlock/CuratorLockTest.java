package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CuratorLockTest {

    // 并发数 / 客户端数
    private static final int QTY = 10;

    // 请求次数
    private static final int REPETITIONS = QTY * 5;

    private static final String LOCK_PATH = "/tutorials/distributed-lock";

    private static TestingCluster cluster;

    // FakeLimitedResource simulates some external resource that can only be access by one process at a time
    private static final FakeLimitedResource resource = new FakeLimitedResource();

    @BeforeAll
    public static void init() throws Exception {
        cluster = new TestingCluster(3);
        cluster.start();
    }

    /**
     * 可重入互斥锁
     */
    @Test
    public void testMutexReentrantLock() throws InterruptedException {

        ExecutorService service = Executors.newFixedThreadPool(QTY);

        for (int i = 0; i < QTY; ++i) {
            final int index = i;
            service.submit(() -> {
                CuratorFramework client = genStartedZKClient();
                try {
                    ExampleClientThatLock example = new ExampleClientThatLock(client, LOCK_PATH, resource, "Client " + index);
                    for (int j = 0; j < REPETITIONS; ++j) {
                        example.doWork(2, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    CloseableUtils.closeQuietly(client);
                }
            });
        }
        service.shutdown();
        service.awaitTermination(5, TimeUnit.MINUTES);
    }

    @Test
    public void tesInterProcessMutex() throws Exception {
        CuratorFramework client = genStartedZKClient();
        InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);
        CuratorFramework client2 = genStartedZKClient();
        InterProcessMutex lock2 = new InterProcessMutex(client2, LOCK_PATH);

        assertTrue(lock.acquire(2L, TimeUnit.SECONDS));
        assertTrue(lock.acquire(2L, TimeUnit.SECONDS));
        assertFalse(lock2.acquire(2L, TimeUnit.SECONDS));
        lock.release();
        lock.release();
        assertTrue(lock2.acquire(2L, TimeUnit.SECONDS));
        lock2.release();
        CloseableUtils.closeQuietly(client);
        CloseableUtils.closeQuietly(client2);
    }

    @Test
    public void testInterProcessSemaphoreMutex() throws Exception {
        CuratorFramework client = genStartedZKClient();
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(client, LOCK_PATH);
        CuratorFramework client2 = genStartedZKClient();
        InterProcessSemaphoreMutex lock2 = new InterProcessSemaphoreMutex(client2, LOCK_PATH);

        assertTrue(lock.acquire(2L, TimeUnit.SECONDS));
        assertFalse(lock.acquire(2L, TimeUnit.SECONDS));
        assertFalse(lock2.acquire(2L, TimeUnit.SECONDS));
        lock.release();
        assertTrue(lock2.acquire(2L, TimeUnit.SECONDS));
        lock2.release();
        CloseableUtils.closeQuietly(client);
        CloseableUtils.closeQuietly(client2);
    }

    @AfterAll
    public static void after() {
        CloseableUtils.closeQuietly(cluster);
    }

    private CuratorFramework genStartedZKClient() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(cluster.getConnectString(), new ExponentialBackoffRetry(1000, 3));
        client.start();
        return client;
    }
}
