package com.chenzhang.thesis.user.convert;

import com.chenzhang.thesis.user.domain.dto.CreateUserDTO;
import com.chenzhang.thesis.user.domain.entity.User;
import com.chenzhang.thesis.user.domain.vo.UserDetailVO;
import com.chenzhang.thesis.user.domain.vo.UserVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 用户对象转换器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserConvert {

    UserVO toUserVO(User user);

    List<UserVO> toUserVOList(List<User> users);

    @AfterMapping
    default void maskPhone(@MappingTarget UserVO vo) {
        String phone = vo.getPhone();
        if (phone != null && phone.length() >= 7) {
            vo.setPhone(phone.substring(0, 3) + "****" + phone.substring(7));
        }
    }

    UserDetailVO toUserDetailVO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "phoneHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "pwdChangedAt", ignore = true)
    @Mapping(target = "lockUntil", ignore = true)
    User toUser(CreateUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "phoneHash", ignore = true)
    @Mapping(target = "schoolId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "pwdChangedAt", ignore = true)
    @Mapping(target = "lockUntil", ignore = true)
    void updateUserFromDto(CreateUserDTO dto, @MappingTarget User user);
}
