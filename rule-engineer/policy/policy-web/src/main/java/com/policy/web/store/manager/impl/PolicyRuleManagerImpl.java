package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyRule;
import com.policy.web.store.manager.PolicyRuleManager;
import com.policy.web.store.mapper.PolicyRuleMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyRuleManagerImpl extends ServiceImpl<PolicyRuleMapper, PolicyRule> implements PolicyRuleManager {

}
