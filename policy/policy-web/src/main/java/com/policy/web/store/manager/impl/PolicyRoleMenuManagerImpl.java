package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyRoleMenu;
import com.policy.web.store.manager.PolicyRoleMenuManager;
import com.policy.web.store.mapper.PolicyRoleMenuMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyRoleMenuManagerImpl extends ServiceImpl<PolicyRoleMenuMapper, PolicyRoleMenu> implements PolicyRoleMenuManager {

}
