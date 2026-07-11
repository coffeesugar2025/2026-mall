package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyConditionGroup;
import com.policy.web.store.manager.PolicyConditionGroupManager;
import com.policy.web.store.mapper.PolicyConditionGroupMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyConditionGroupManagerImpl extends ServiceImpl<PolicyConditionGroupMapper, PolicyConditionGroup> implements PolicyConditionGroupManager {

}
