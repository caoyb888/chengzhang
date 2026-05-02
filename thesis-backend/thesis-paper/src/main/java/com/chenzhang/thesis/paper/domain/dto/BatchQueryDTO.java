package com.chenzhang.thesis.paper.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 批次列表查询入参
 */
@Data
public class BatchQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String status;
    private String paperType;
    private String keyword;
}
