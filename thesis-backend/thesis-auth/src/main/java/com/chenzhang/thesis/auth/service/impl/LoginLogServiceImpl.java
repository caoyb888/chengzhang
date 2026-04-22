package com.chenzhang.thesis.auth.service.impl;

import com.chenzhang.thesis.auth.domain.entity.LoginLog;
import com.chenzhang.thesis.auth.mapper.LoginLogMapper;
import com.chenzhang.thesis.auth.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Override
    public void saveLoginLog(LoginLog loginLog) {
        loginLogMapper.insert(loginLog);
    }
}
