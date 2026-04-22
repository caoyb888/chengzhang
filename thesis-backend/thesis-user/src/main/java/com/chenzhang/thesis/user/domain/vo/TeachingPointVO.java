package com.chenzhang.thesis.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教学点出参
 */
@Data
public class TeachingPointVO {

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
}
