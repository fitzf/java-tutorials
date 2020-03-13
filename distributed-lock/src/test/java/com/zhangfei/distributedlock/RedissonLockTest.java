package com.zhangfei.distributedlock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedissonLockTest extends BaseTest {

    private static final String LOCK_NAME = "example_lock";

    private static String REDIS_SINGLE_SERVER;

    @BeforeAll
    private static void setUp() {
        REDIS_SINGLE_SERVER = System.getProperty("REDIS_SINGLE_SERVER", "127.0.0.1:6379");
    }

    @Test
    public void testReentrantLock() throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(QTY);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < QTY; ++i) {
            final int index = i;
            tasks.add(() -> {
                Config config = new Config();
                config.useSingleServer().setAddress("redis://" + REDIS_SINGLE_SERVER);
                RedissonClient client = Redisson.create(config);
                try {
                    RedissonLockExampleClient example = new RedissonLockExampleClient(client, LOCK_NAME, resource, "Client " + index);
                    for (int j = 0; j < REPETITIONS; ++j) {
                        example.doWork(100, 10, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw e;
                } finally {
                    client.shutdown();
                }
                return null;
            });
        }
        List<Future<Void>> results = service.invokeAll(tasks);
        service.shutdown();
        service.awaitTermination(10, TimeUnit.MINUTES);
        for (Future<Void> voidFuture : results) {
            voidFuture.get();
        }
    }
}
