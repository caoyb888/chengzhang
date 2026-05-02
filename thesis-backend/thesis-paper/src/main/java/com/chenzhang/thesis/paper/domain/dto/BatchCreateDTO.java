package com.chenzhang.thesis.paper.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 创建批次入参
 */
@Data
public class BatchCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "批次名称不能为空")
    private String name;

    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @NotBlank(message = "学期不能为空")
    private String semester;

    @NotBlank(message = "论文类型不能为空")
    private String paperType;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String description;
}
