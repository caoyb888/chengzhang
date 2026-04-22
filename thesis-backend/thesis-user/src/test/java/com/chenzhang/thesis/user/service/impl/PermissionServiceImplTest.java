package com.chenzhang.thesis.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenzhang.thesis.user.domain.entity.Permission;
import com.chenzhang.thesis.user.domain.vo.PermissionVO;
import com.chenzhang.thesis.user.mapper.PermissionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 测试计划 §4.2：权限树 Service 测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl 权限服务测试")
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    @DisplayName("TC-PERM-001: 权限树 — 正确构建父子层级")
    void getPermissionTree_returnsTreeStructure() {
        Permission root = new Permission();
        root.setId(1L);
        root.setPermCode("user");
        root.setPermName("用户管理");
        root.setParentId(null);
        root.setSortOrder(1);

        Permission child1 = new Permission();
        child1.setId(2L);
        child1.setPermCode("user:create");
        child1.setPermName("新增用户");
        child1.setParentId(1L);
        child1.setSortOrder(1);

        Permission child2 = new Permission();
        child2.setId(3L);
        child2.setPermCode("user:update");
        child2.setPermName("编辑用户");
        child2.setParentId(1L);
        child2.setSortOrder(2);

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(root, child1, child2));

        List<PermissionVO> tree = permissionService.getPermissionTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getChildren()).hasSize(2);
        assertThat(tree.get(0).getChildren().get(0).getPermCode()).isEqualTo("user:create");
        assertThat(tree.get(0).getChildren().get(1).getPermCode()).isEqualTo("user:update");
    }

    @Test
    @DisplayName("TC-PERM-002: 权限树 — 空数据返回空列表")
    void getPermissionTree_empty_returnsEmpty() {
        when(permissionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        List<PermissionVO> tree = permissionService.getPermissionTree();
        assertThat(tree).isEmpty();
    }

    @Test
    @DisplayName("TC-PERM-003: 权限树 — 多级嵌套正确")
    void getPermissionTree_multiLevel_returnsCorrectHierarchy() {
        Permission l1 = new Permission();
        l1.setId(1L);
        l1.setPermCode("system");
        l1.setParentId(null);
        l1.setSortOrder(1);

        Permission l2 = new Permission();
        l2.setId(2L);
        l2.setPermCode("system:user");
        l2.setParentId(1L);
        l2.setSortOrder(1);

        Permission l3 = new Permission();
        l3.setId(3L);
        l3.setPermCode("system:user:create");
        l3.setParentId(2L);
        l3.setSortOrder(1);

        when(permissionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(l1, l2, l3));

        List<PermissionVO> tree = permissionService.getPermissionTree();

        assertThat(tree.get(0).getChildren().get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getChildren().get(0).getPermCode())
                .isEqualTo("system:user:create");
    }
}
