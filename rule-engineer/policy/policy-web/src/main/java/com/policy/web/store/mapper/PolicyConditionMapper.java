package com.policy.web.store.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyCondition;


public interface PolicyConditionMapper extends BaseMapper<PolicyCondition> {

    /**
     * 更新引用此规则的条件到待发布
     *
     * @param conditionId 条件id
     */
    void updateRuleWaitPublish(Integer conditionId);

    /**
     * 更新引用此规则集的条件到待发布
     *
     * @param conditionId 条件id
     */
    void updateRuleSetWaitPublish(Integer conditionId);
}
