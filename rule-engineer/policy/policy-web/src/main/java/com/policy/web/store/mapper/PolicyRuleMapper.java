package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyRule;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author 
 * @since 2020-07-15
 */
public interface PolicyRuleMapper extends BaseMapper<PolicyRule> {

    /**
     * 根据id更新
     *
     * @param policyRule 规则信息
     * @return int
     */
    int updateRuleById(PolicyRule policyRule);

}
