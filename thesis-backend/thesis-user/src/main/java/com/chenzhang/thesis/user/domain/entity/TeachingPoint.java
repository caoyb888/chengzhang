package com.chenzhang.thesis.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教学点实体
 */
@Data
@TableName("teaching_point")
public class TeachingPoint {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long schoolId;

    private String name;

    private String code;

    private String contactName;

    private String contactPhone;

    private String dataScope;

    private String status;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
