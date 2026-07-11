package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyRuleSetRule;
import com.policy.web.store.manager.PolicyRuleSetRuleManager;
import com.policy.web.store.mapper.PolicyRuleSetRuleMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyRuleSetRuleManagerImpl extends ServiceImpl<PolicyRuleSetRuleMapper, PolicyRuleSetRule> implements PolicyRuleSetRuleManager {

}
