package com.chenzhang.thesis.paper.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 批次列表出参
 */
@Data
public class BatchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String name;
    private String academicYear;
    private String semester;
    private String paperType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String status;
    private Integer totalStudents;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
