package com.chenzhang.thesis.user.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserImportService {

    Map<String, Object> submitImportTask(String importType, MultipartFile file, Long operatorId, Long schoolId, Long batchId);

    Map<String, Object> getProgress(String taskId);
}
