package com.chenzhang.thesis.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 测试计划 §4.4：SnowflakeIdGenerator 唯一性与并发测试
 * TC-SNOWFLAKE-001: 1000 并发调用全部唯一
 */
@DisplayName("SnowflakeIdGenerator 雪花 ID 生成器测试")
class SnowflakeIdGeneratorTest {

    private SnowflakeIdGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new SnowflakeIdGenerator();
    }

    @Test
    @DisplayName("TC-SNOW-001: 单线程连续生成 10000 个 ID — 全部唯一")
    void nextId_singleThread_10000ids_allUnique() {
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 10_000; i++) {
            ids.add(generator.nextId());
        }
        assertThat(ids).hasSize(10_000);
    }

    @Test
    @DisplayName("TC-SNOW-002: 10 线程并发生成共 1000 个 ID — 全部唯一（核心并发测试）")
    void nextId_10Threads_1000ids_allUnique() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 100;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // 齐头并进
                    for (int i = 0; i < idsPerThread; i++) {
                        ids.add(generator.nextId());
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown(); // 同时开始
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();
        assertThat(errors.get()).isZero();
        assertThat(ids).hasSize(threadCount * idsPerThread);
    }

    @Test
    @DisplayName("TC-SNOW-003: 生成的 ID 均为正数（符号位=0）")
    void nextId_alwaysPositive() {
        for (int i = 0; i < 100; i++) {
            assertThat(generator.nextId()).isPositive();
        }
    }

    @Test
    @DisplayName("TC-SNOW-004: ID 单调递增（时间戳位保证顺序性）")
    void nextId_isMonotonicallyIncreasing() {
        long prev = generator.nextId();
        for (int i = 0; i < 999; i++) {
            long curr = generator.nextId();
            assertThat(curr).isGreaterThan(prev);
            prev = curr;
        }
    }

    @Test
    @DisplayName("TC-SNOW-005: dataCenterId 超出范围 [0,31] 抛出 IllegalArgumentException")
    void constructor_invalidDataCenterId_throwsIllegalArgument() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(32, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dataCenterId");
        assertThatThrownBy(() -> new SnowflakeIdGenerator(-1, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("TC-SNOW-006: machineId 超出范围 [0,31] 抛出 IllegalArgumentException")
    void constructor_invalidMachineId_throwsIllegalArgument() {
        assertThatThrownBy(() -> new SnowflakeIdGenerator(1, 32))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("machineId");
    }

    @Test
    @DisplayName("TC-SNOW-007: 不同 dataCenterId 和 machineId 组合不产生相同 ID")
    void differentMachineConfigs_doNotCollide() throws InterruptedException {
        SnowflakeIdGenerator g1 = new SnowflakeIdGenerator(1, 1);
        SnowflakeIdGenerator g2 = new SnowflakeIdGenerator(1, 2);
        SnowflakeIdGenerator g3 = new SnowflakeIdGenerator(2, 1);

        Set<Long> all = ConcurrentHashMap.newKeySet();
        for (int i = 0; i < 1000; i++) {
            all.add(g1.nextId());
            all.add(g2.nextId());
            all.add(g3.nextId());
        }
        assertThat(all).hasSize(3000); // 3个生成器各1000个，无碰撞
    }

    @RepeatedTest(5)
    @DisplayName("TC-SNOW-008: 生成的 ID 大于自定义 EPOCH（2024-04-20）")
    void nextId_isGreaterThanEpoch() {
        // EPOCH = 1713600000000L，ID 中时间戳部分应大于此值
        long id = generator.nextId();
        long timestamp = (id >> 22) + 1713600000000L; // 反推时间戳
        assertThat(timestamp).isGreaterThanOrEqualTo(1713600000000L);
    }
}
