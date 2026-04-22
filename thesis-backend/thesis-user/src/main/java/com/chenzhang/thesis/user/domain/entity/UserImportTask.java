package com.chenzhang.thesis.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户批量导入任务表
 */
@Data
@TableName("user_import_task")
public class UserImportTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskId;
    private Long schoolId;
    private Long operatorId;
    private String importType;
    private String fileUrl;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String errorFileUrl;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
