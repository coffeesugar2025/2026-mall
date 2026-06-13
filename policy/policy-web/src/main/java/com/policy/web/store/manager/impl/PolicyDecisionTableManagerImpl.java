package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyDecisionTable;
import com.policy.web.store.manager.PolicyDecisionTableManager;
import com.policy.web.store.mapper.PolicyDecisionTableMapper;
import org.springframework.stereotype.Service;


@Service
public class PolicyDecisionTableManagerImpl extends ServiceImpl<PolicyDecisionTableMapper, PolicyDecisionTable> implements PolicyDecisionTableManager {

}
