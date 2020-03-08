package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.utils.CloseableUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorLockTest {

    // 并发数 / 客户端数
    private static final int QTY = 10;

    // 请求次数
    private static final int REPETITIONS = QTY * 5;

    private static final String LOCK_PATH = "/distributed-lock";

    private TestingCluster cluster;

    // FakeLimitedResource simulates some external resource that can only be access by one process at a time
    private static final FakeLimitedResource resource = new FakeLimitedResource();

    @BeforeEach
    public void init() throws Exception {
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
                CuratorFramework client = CuratorFrameworkFactory.newClient(cluster.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                try {
                    client.start();

                    ExampleClientThatLock example = new ExampleClientThatLock(client, LOCK_PATH, resource, "Client " + index);
                    for (int j = 0; j < REPETITIONS; ++j) {
                        example.doWork(5, TimeUnit.SECONDS);
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

    @AfterEach
    public void after() {
        CloseableUtils.closeQuietly(cluster);
    }
}
