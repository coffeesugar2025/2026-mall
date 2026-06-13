package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.policy.web.store.entity.PolicyConditionGroupCondition;
import com.policy.web.store.manager.PolicyConditionGroupConditionManager;
import com.policy.web.store.mapper.PolicyConditionGroupConditionMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyConditionGroupConditionManagerImpl extends ServiceImpl<PolicyConditionGroupConditionMapper, PolicyConditionGroupCondition> implements PolicyConditionGroupConditionManager {

}
