package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyMenu;
import com.policy.web.store.manager.PolicyMenuManager;
import com.policy.web.store.mapper.PolicyMenuMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyMenuManagerImpl extends ServiceImpl<PolicyMenuMapper, PolicyMenu> implements PolicyMenuManager {

}
