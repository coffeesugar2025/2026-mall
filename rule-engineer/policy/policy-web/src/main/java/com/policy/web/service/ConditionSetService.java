package com.policy.web.service;

import cn.hutool.core.collection.CollUtil;
import com.policy.web.vo.condition.*;
import com.policy.core.condition.Condition;
import com.policy.core.condition.ConditionGroup;
import com.policy.core.condition.ConditionSet;
import com.policy.core.condition.Operator;
import com.policy.web.store.entity.PolicyCondition;
import com.policy.web.store.entity.PolicyConditionGroup;
import com.policy.web.store.entity.PolicyConditionGroupCondition;
import com.policy.web.store.manager.PolicyConditionGroupConditionManager;
import com.policy.web.store.manager.PolicyConditionGroupManager;
import com.policy.web.store.manager.PolicyConditionManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2021/1/17
 * @since 1.0.0
 */
@Service
public class ConditionSetService {

    @Resource
    private PolicyConditionGroupManager policyConditionGroupManager;
    @Resource
    private PolicyConditionGroupConditionManager policyConditionGroupConditionManager;
    @Resource
    private PolicyConditionManager policyConditionManager;
    @Resource
    private ValueResolve valueResolve;

    /**
     * 获取规则配置条件集，懒得写的，待优化
     *
     * @param conditionGroup 条件组配置
     * @return 条件集
     */
    public ConditionSet loadConditionSet(List<ConditionGroupConfig> conditionGroup) {
        ConditionSet conditionSet = new ConditionSet();
        for (ConditionGroupConfig conditionGroupConfig : conditionGroup) {
            ConditionGroup conditionGroups = new ConditionGroup();
            conditionGroups.setId(conditionGroupConfig.getId());
            conditionGroups.setName(conditionGroupConfig.getName());
            conditionGroups.setOrderNo(conditionGroupConfig.getOrderNo());
            List<ConditionGroupCondition> conditionGroupCondition = conditionGroupConfig.getConditionGroupCondition();
            if (CollUtil.isNotEmpty(conditionGroupCondition)) {
                for (ConditionGroupCondition groupCondition : conditionGroupCondition) {
                    ConditionBody conditionBody = groupCondition.getCondition();
                    Condition condition = new Condition();
                    condition.setId(conditionBody.getId());
                    condition.setName(conditionBody.getName());
                    condition.setOrderNo(groupCondition.getOrderNo());
                    ConfigBean config = conditionBody.getConfig();
                    ConfigValue leftValue = config.getLeftValue();
                    condition.setLeftValue(this.valueResolve.getValue(leftValue.getType(), leftValue.getValueType(), leftValue.getValue()));
                    condition.setOperator(Operator.getByName(config.getSymbol()));
                    ConfigValue rightValue = config.getRightValue();
                    condition.setRightValue(this.valueResolve.getValue(rightValue.getType(), rightValue.getValueType(), rightValue.getValue()));
                    conditionGroups.addCondition(condition);
                }
            }
            // 条件组内没有条件，生成待发布json 则不再处理
            if (CollUtil.isNotEmpty(conditionGroups.getConditions())) {
                conditionSet.addConditionGroup(conditionGroups);
            }
        }
        return conditionSet;
    }

    /**
     * 获取规则配置条件集，懒得写的，待优化
     *
     * @param ruleId 规则id
     * @return 条件集
     */
    public ConditionSet loadConditionSet(Integer ruleId) {
        List<PolicyConditionGroup> conditionGroups = this.policyConditionGroupManager.lambdaQuery()
                .eq(PolicyConditionGroup::getRuleId, ruleId)
                .orderByAsc(PolicyConditionGroup::getOrderNo)
                .list();
        if (CollUtil.isEmpty(conditionGroups)) {
            return new ConditionSet();
        }
        // 加载所有的用到的条件组条件
        Set<Integer> conditionGroupIds = conditionGroups.stream().map(PolicyConditionGroup::getId).collect(Collectors.toSet());
        Map<Integer, List<PolicyConditionGroupCondition>> conditionGroupConditionMaps = this.policyConditionGroupConditionManager.lambdaQuery()
                .in(PolicyConditionGroupCondition::getConditionGroupId, conditionGroupIds)
                .orderByAsc(PolicyConditionGroupCondition::getOrderNo)
                .list()
                .stream().collect(Collectors.groupingBy(PolicyConditionGroupCondition::getConditionGroupId));
        Set<Integer> conditionIds = conditionGroupConditionMaps.values().stream().flatMap(Collection::stream).map(PolicyConditionGroupCondition::getConditionId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(conditionIds)) {
            return new ConditionSet();
        }
        List<PolicyCondition> policyConditions = policyConditionManager.lambdaQuery().in(PolicyCondition::getId, conditionIds).list();
        Map<Integer, PolicyCondition> conditionMap = policyConditions.stream().collect(Collectors.toMap(PolicyCondition::getId, Function.identity()));
        ConditionSet conditionSet = new ConditionSet();
        for (PolicyConditionGroup group : conditionGroups) {
            ConditionGroup conditionGroup = new ConditionGroup();
            conditionGroup.setId(group.getId());
            conditionGroup.setOrderNo(group.getOrderNo());
            List<PolicyConditionGroupCondition> groupConditions = conditionGroupConditionMaps.get(group.getId());
            if (CollUtil.isNotEmpty(groupConditions)) {
                for (PolicyConditionGroupCondition groupCondition : groupConditions) {
                    PolicyCondition engineCondition = conditionMap.get(groupCondition.getConditionId());
                    Condition condition = new Condition();
                    condition.setId(engineCondition.getId());
                    condition.setName(engineCondition.getName());
                    condition.setOrderNo(groupCondition.getOrderNo());
                    condition.setLeftValue(valueResolve.getValue(engineCondition.getLeftType(), engineCondition.getLeftValueType(), engineCondition.getLeftValue()));
                    condition.setOperator(Operator.getByName(engineCondition.getSymbol()));
                    condition.setRightValue(valueResolve.getValue(engineCondition.getRightType(), engineCondition.getRightValueType(), engineCondition.getRightValue()));
                    conditionGroup.addCondition(condition);
                }
                conditionSet.addConditionGroup(conditionGroup);
            }
        }
        return conditionSet;
    }
}

