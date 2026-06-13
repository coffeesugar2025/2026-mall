package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyRole;
import com.policy.web.store.manager.PolicyRoleManager;
import com.policy.web.store.mapper.PolicyRoleMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyRoleManagerImpl extends ServiceImpl<PolicyRoleMapper, PolicyRole> implements PolicyRoleManager {

}
