package com.chenzhang.thesis.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenzhang.thesis.user.domain.entity.Permission;
import com.chenzhang.thesis.user.domain.vo.PermissionVO;
import com.chenzhang.thesis.user.mapper.PermissionMapper;
import com.chenzhang.thesis.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionVO> getPermissionTree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getIsDeleted, 0)
                        .orderByAsc(Permission::getSortOrder)
        );
        Map<Long, List<PermissionVO>> parentMap = all.stream()
                .map(this::toPermissionVO)
                .collect(HashMap::new,
                        (m, p) -> m.computeIfAbsent(p.getParentId(), k -> new ArrayList<>()).add(p),
                        HashMap::putAll);

        List<PermissionVO> roots = parentMap.getOrDefault(null, List.of());
        roots.forEach(root -> fillChildren(root, parentMap));
        return roots;
    }

    private void fillChildren(PermissionVO node, Map<Long, List<PermissionVO>> parentMap) {
        List<PermissionVO> children = parentMap.getOrDefault(node.getId(), new ArrayList<>());
        children.sort(Comparator.comparingInt(PermissionVO::getSortOrder));
        node.setChildren(children);
        children.forEach(child -> fillChildren(child, parentMap));
    }

    private PermissionVO toPermissionVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setPermCode(permission.getPermCode());
        vo.setPermName(permission.getPermName());
        vo.setPermType(permission.getPermType());
        vo.setModule(permission.getModule());
        vo.setParentId(permission.getParentId());
        vo.setRoutePath(permission.getRoutePath());
        vo.setIcon(permission.getIcon());
        vo.setSortOrder(permission.getSortOrder());
        vo.setDescription(permission.getDescription());
        return vo;
    }
}
