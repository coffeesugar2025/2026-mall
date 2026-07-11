package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyGeneralRule;


public interface PolicyGeneralRuleMapper extends BaseMapper<PolicyGeneralRule> {

    /**
     * 根据id更新
     *
     * @param policyGeneralRule 规则信息
     */
    void updateRuleById(PolicyGeneralRule policyGeneralRule);

}
