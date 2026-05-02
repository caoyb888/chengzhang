package com.chenzhang.thesis.paper.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 批次详情出参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchDetailVO extends BatchVO {

    @Serial
    private static final long serialVersionUID = 1L;

    private String description;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archivedAt;
}
