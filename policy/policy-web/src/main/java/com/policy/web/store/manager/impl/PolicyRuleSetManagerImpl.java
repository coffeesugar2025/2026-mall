package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyRuleSet;
import com.policy.web.store.manager.PolicyRuleSetManager;
import com.policy.web.store.mapper.PolicyRuleSetMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyRuleSetManagerImpl extends ServiceImpl<PolicyRuleSetMapper, PolicyRuleSet> implements PolicyRuleSetManager {

}
