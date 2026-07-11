package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyFunction;
import com.policy.web.store.manager.PolicyFunctionManager;
import com.policy.web.store.mapper.PolicyFunctionMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2020-07-15
 */
@Service
public class PolicyFunctionManagerImpl extends ServiceImpl<PolicyFunctionMapper, PolicyFunction> implements PolicyFunctionManager {

}
