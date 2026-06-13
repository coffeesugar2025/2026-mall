package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicySystemLog;
import com.policy.web.store.manager.PolicySystemLogManager;
import com.policy.web.store.mapper.PolicySystemLogMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicySystemLogManagerImpl extends ServiceImpl<PolicySystemLogMapper, PolicySystemLog> implements PolicySystemLogManager {

}
