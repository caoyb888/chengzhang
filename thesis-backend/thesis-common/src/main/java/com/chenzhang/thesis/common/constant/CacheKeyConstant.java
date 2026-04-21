package com.chenzhang.thesis.common.constant;

/**
 * 缓存 Key 命名规范（对应 CLAUDE.md §5.6）
 * 使用 String.format(KEY, args) 生成实际 Key
 */
public final class CacheKeyConstant {

    private CacheKeyConstant() {}

    /** auth:token:{userId}  TTL 7d */
    public static final String AUTH_TOKEN      = "auth:token:%s";
    /** auth:perm:{userId}   TTL 1h */
    public static final String AUTH_PERM       = "auth:perm:%s";
    /** paper:status:{paperId}  TTL 30min */
    public static final String PAPER_STATUS    = "paper:status:%s";
    /** stat:batch:{batchId}  TTL 5min */
    public static final String STAT_BATCH      = "stat:batch:%s";
    /** sms:{phone}  TTL 5min */
    public static final String SMS_CODE        = "sms:%s";
    /** import:task:{taskId}  TTL 24h */
    public static final String IMPORT_TASK     = "import:task:%s";
    /** topic:lock:{batchId}:{hash}  分布式锁 */
    public static final String TOPIC_LOCK      = "topic:lock:%s:%s";
}
