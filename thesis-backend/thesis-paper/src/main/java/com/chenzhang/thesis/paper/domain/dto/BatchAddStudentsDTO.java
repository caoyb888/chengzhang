package com.chenzhang.thesis.paper.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量添加学生入参
 */
@Data
public class BatchAddStudentsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "学生ID列表不能为空")
    private List<@NotNull(message = "学生ID不能为空") Long> studentIds;
}
