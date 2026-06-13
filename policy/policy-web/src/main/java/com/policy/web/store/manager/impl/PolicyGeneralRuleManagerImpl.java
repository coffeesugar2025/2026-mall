package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyGeneralRule;
import com.policy.web.store.manager.PolicyGeneralRuleManager;
import com.policy.web.store.mapper.PolicyGeneralRuleMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyGeneralRuleManagerImpl extends ServiceImpl<PolicyGeneralRuleMapper, PolicyGeneralRule> implements PolicyGeneralRuleManager {

}
