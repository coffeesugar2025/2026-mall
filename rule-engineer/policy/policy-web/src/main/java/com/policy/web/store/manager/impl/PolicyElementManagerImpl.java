package com.policy.web.store.manager.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyElement;
import com.policy.web.store.manager.PolicyElementManager;
import com.policy.web.store.mapper.PolicyElementMapper;
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
public class PolicyElementManagerImpl extends ServiceImpl<PolicyElementMapper, PolicyElement> implements PolicyElementManager {

}
