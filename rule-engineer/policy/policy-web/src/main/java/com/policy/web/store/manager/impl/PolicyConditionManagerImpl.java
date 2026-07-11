package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyCondition;
import com.policy.web.store.manager.PolicyConditionManager;
import com.policy.web.store.mapper.PolicyConditionMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyConditionManagerImpl extends ServiceImpl<PolicyConditionMapper, PolicyCondition> implements PolicyConditionManager {

}
