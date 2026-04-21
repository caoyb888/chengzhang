package com.chenzhang.thesis.common.constant;

/**
 * 用户类型常量（对应数据库 user.user_type 字段枚举值）
 */
public final class UserTypeConstant {

    private UserTypeConstant() {}

    public static final String SCHOOL_ADMIN = "SCHOOL_ADMIN";  // 学校管理员
    public static final String POINT_ADMIN  = "POINT_ADMIN";   // 教学点管理员
    public static final String TEACHER      = "TEACHER";       // 指导教师
    public static final String ASSISTANT    = "ASSISTANT";     // 辅助指导教师
    public static final String STUDENT      = "STUDENT";       // 学生
}
