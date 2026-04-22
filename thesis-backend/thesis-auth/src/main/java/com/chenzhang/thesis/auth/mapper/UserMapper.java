package com.chenzhang.thesis.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenzhang.thesis.auth.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM thesis_user.user WHERE username = #{username} AND is_deleted = 0 LIMIT 1")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM thesis_user.user WHERE phone_hash = #{phoneHash} AND is_deleted = 0 LIMIT 1")
    User selectByPhoneHash(@Param("phoneHash") String phoneHash);

    @Select("SELECT p.perm_code FROM thesis_user.permission p " +
            "INNER JOIN thesis_user.role_permission rp ON p.id = rp.perm_id " +
            "INNER JOIN thesis_user.user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.is_deleted = 0 AND rp.is_deleted = 0 AND ur.is_deleted = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
