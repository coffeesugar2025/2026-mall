package com.policy.web.store.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.policy.web.store.entity.PolicyUser;
import com.policy.web.store.manager.PolicyUserManager;
import com.policy.web.store.mapper.PolicyUserMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 规则引擎用户表 服务实现类
 * </p>
 *
 * @author 
 * @since 2020-09-24
 */
@Service
public class PolicyUserManagerImpl extends ServiceImpl<PolicyUserMapper, PolicyUser> implements PolicyUserManager {

}
