package com.chenzhang.thesis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.auth.domain.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    @Select("SELECT * FROM auth_refresh_token WHERE token_value = #{tokenValue} AND is_deleted = 0 LIMIT 1")
    RefreshToken selectByTokenValue(@Param("tokenValue") String tokenValue);
}
