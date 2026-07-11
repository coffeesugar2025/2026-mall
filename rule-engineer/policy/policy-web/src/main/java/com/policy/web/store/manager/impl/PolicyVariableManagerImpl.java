package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyVariable;
import com.policy.web.store.manager.PolicyVariableManager;
import com.policy.web.store.mapper.PolicyVariableMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2020-07-14
 */
@Service
public class PolicyVariableManagerImpl extends ServiceImpl<PolicyVariableMapper, PolicyVariable> implements PolicyVariableManager {

}
