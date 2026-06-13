package com.policy.web.service.impl;


import com.policy.web.service.RoleService;
import com.policy.web.store.entity.PolicyRole;
import com.policy.web.store.manager.PolicyRoleManager;
import com.policy.web.store.mapper.PolicyRoleMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2019/10/22
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private PolicyRoleManager bootRoleManager;
    @Resource
    private PolicyRoleMapper bootRoleMapper;

    /**
     * 根据role code查询对应的角色路径
     *
     * @param codes Codes
     * @return RolePath
     */
    @Override
    public Set<String> listRoleIdByCodes(Collection<String> codes) {
        return bootRoleManager.lambdaQuery()
                .select(PolicyRole::getRolePath).in(PolicyRole::getCode, codes)
                .list().stream().map(PolicyRole::getRolePath).collect(Collectors.toSet());
    }

    /**
     * 根据用户id查询此用户的角色
     *
     * @param userId 用户id
     * @return List
     */
    @Override
    public List<PolicyRole> listRoleByUserId(Integer userId) {
        return bootRoleMapper.listRoleByUserId(userId);
    }
}
