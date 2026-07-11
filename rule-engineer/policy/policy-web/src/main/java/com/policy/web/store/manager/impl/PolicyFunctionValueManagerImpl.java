package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyFunctionValue;
import com.policy.web.store.manager.PolicyFunctionValueManager;
import com.policy.web.store.mapper.PolicyFunctionValueMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyFunctionValueManagerImpl extends ServiceImpl<PolicyFunctionValueMapper, PolicyFunctionValue> implements PolicyFunctionValueManager {

}
