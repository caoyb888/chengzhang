package com.chenzhang.thesis.common.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成器
 * 64位：符号位(1) + 时间戳(41) + 数据中心(5) + 机器(5) + 序列号(12)
 * 可保证在同一毫秒内生成 4096 个唯一 ID
 */
@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1713600000000L; // 2024-04-20 00:00:00 UTC

    private static final int DATA_CENTER_BITS = 5;
    private static final int MACHINE_BITS     = 5;
    private static final int SEQUENCE_BITS    = 12;

    private static final long MAX_DATA_CENTER = ~(-1L << DATA_CENTER_BITS); // 31
    private static final long MAX_MACHINE     = ~(-1L << MACHINE_BITS);     // 31
    private static final long MAX_SEQUENCE    = ~(-1L << SEQUENCE_BITS);    // 4095

    private static final int  MACHINE_SHIFT     = SEQUENCE_BITS;
    private static final int  DATA_CENTER_SHIFT  = SEQUENCE_BITS + MACHINE_BITS;
    private static final int  TIMESTAMP_SHIFT    = SEQUENCE_BITS + MACHINE_BITS + DATA_CENTER_BITS;

    private final long dataCenterId;
    private final long machineId;
    private long sequence  = 0L;
    private long lastStamp = -1L;

    public SnowflakeIdGenerator() {
        this(1L, 1L);
    }

    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId 超出范围 [0, " + MAX_DATA_CENTER + "]");
        }
        if (machineId > MAX_MACHINE || machineId < 0) {
            throw new IllegalArgumentException("machineId 超出范围 [0, " + MAX_MACHINE + "]");
        }
        this.dataCenterId = dataCenterId;
        this.machineId    = machineId;
    }

    public synchronized long nextId() {
        long now = System.currentTimeMillis();
        if (now < lastStamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID，回拨时长=" + (lastStamp - now) + "ms");
        }
        if (now == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                now = waitNextMillis(now);
            }
        } else {
            sequence = 0L;
        }
        lastStamp = now;

        return ((now - EPOCH) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (machineId    << MACHINE_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long current) {
        long ts = System.currentTimeMillis();
        while (ts <= current) {
            ts = System.currentTimeMillis();
        }
        return ts;
    }
}
