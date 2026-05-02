package com.chenzhang.thesis.paper.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Feign 调用用户服务出参（精简字段）
 */
@Data
public class FeignUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long schoolId;
    private Long teachingPointId;
    private String realName;
    private String userType;
    private String major;
    private String status;
}
