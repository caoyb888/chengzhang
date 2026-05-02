package com.chenzhang.thesis.paper.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量添加学生结果出参
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchAddStudentsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer addedCount;
    private Integer skippedCount;
    private List<String> skipReasons;
}
