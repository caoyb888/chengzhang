package com.chenzhang.thesis.auth.service;

import com.chenzhang.thesis.auth.domain.entity.LoginLog;

public interface LoginLogService {

    void saveLoginLog(LoginLog loginLog);
}
