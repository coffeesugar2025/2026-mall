package com.policy.web.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.policy.web.store.entity.*;
import com.policy.core.condition.Condition;
import com.policy.core.condition.ConditionGroup;
import com.policy.web.service.ConditionService;
import com.policy.web.service.PolicyConditionGroupService;
import com.policy.web.service.ValueResolve;
import com.policy.web.store.manager.PolicyConditionGroupConditionManager;
import com.policy.web.store.manager.PolicyConditionGroupManager;
import com.policy.web.store.manager.PolicyConditionManager;
import com.policy.web.vo.condition.ConditionBody;
import com.policy.web.vo.condition.ConditionGroupCondition;
import com.policy.web.vo.condition.ConditionGroupConfig;
import com.policy.web.vo.condition.ConfigBean;
import com.policy.web.vo.condition.group.SaveOrUpdateConditionGroup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/28
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class PolicyConditionGroupServiceImpl implements PolicyConditionGroupService {

    @Resource
    private PolicyConditionGroupManager policyConditionGroupManager;
    @Resource
    private PolicyConditionGroupConditionManager policyConditionGroupConditionManager;
    @Resource
    private ConditionService conditionService;
    @Resource
    private PolicyConditionManager policyConditionManager;
    @Resource
    private ValueResolve valueResolve;

    /**
     * 保存或者更新条件组
     *
     * @param saveOrUpdateConditionGroup 条件组信息
     * @return int
     */
    @Override
    public Integer saveOrUpdateConditionGroup(SaveOrUpdateConditionGroup saveOrUpdateConditionGroup) {
        PolicyConditionGroup engineConditionGroup = new PolicyConditionGroup();
        engineConditionGroup.setId(saveOrUpdateConditionGroup.getId());
        engineConditionGroup.setName(saveOrUpdateConditionGroup.getName());
        engineConditionGroup.setRuleId(saveOrUpdateConditionGroup.getRuleId());
        engineConditionGroup.setOrderNo(saveOrUpdateConditionGroup.getOrderNo());
        this.policyConditionGroupManager.saveOrUpdate(engineConditionGroup);
        return engineConditionGroup.getId();
    }

    /**
     * 保存条件组
     *
     * @param ruleId         规则id
     * @param conditionGroup 条件组信息
     */
    @Override
    public void saveConditionGroup(Integer ruleId, List<ConditionGroupConfig> conditionGroup) {
        if (CollUtil.isEmpty(conditionGroup)) {
            return;
        }
        List<PolicyConditionGroupCondition> policyConditionGroupConditions = new LinkedList<>();
        for (ConditionGroupConfig groupConfig : conditionGroup) {
            PolicyConditionGroup engineConditionGroup = new PolicyConditionGroup();
            engineConditionGroup.setName(groupConfig.getName());
            engineConditionGroup.setRuleId(ruleId);
            engineConditionGroup.setOrderNo(groupConfig.getOrderNo());
            this.policyConditionGroupManager.save(engineConditionGroup);
            List<ConditionGroupCondition> conditionGroupConditions = groupConfig.getConditionGroupCondition();
            if (CollUtil.isNotEmpty(conditionGroupConditions)) {
                for (ConditionGroupCondition conditionGroupCondition : conditionGroupConditions) {
                    PolicyConditionGroupCondition policyConditionGroupCondition = new PolicyConditionGroupCondition();
                    policyConditionGroupCondition.setConditionId(conditionGroupCondition.getCondition().getId());
                    policyConditionGroupCondition.setConditionGroupId(engineConditionGroup.getId());
                    policyConditionGroupCondition.setOrderNo(conditionGroupCondition.getOrderNo());
                    policyConditionGroupConditions.add(policyConditionGroupCondition);
                }
            }
        }
        if (CollUtil.isNotEmpty(policyConditionGroupConditions)) {
            this.policyConditionGroupConditionManager.saveBatch(policyConditionGroupConditions);
        }
    }

    /**
     * 删除条件组
     *
     * @param id 条件组id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        return this.policyConditionGroupManager.removeById(id);
    }


    /**
     * 删除规则条件组信息
     *
     * @param ruleIds 规则ids
     */
    @Async
    @Override
    public void removeConditionGroupByRuleIds(List<Integer> ruleIds) {
        List<PolicyConditionGroup> engineConditionGroups = policyConditionGroupManager.lambdaQuery()
                .in(PolicyConditionGroup::getRuleId, ruleIds)
                .list();
        if (CollUtil.isNotEmpty(engineConditionGroups)) {
            List<Integer> engineConditionGroupIds = engineConditionGroups.stream().map(PolicyConditionGroup::getId).collect(Collectors.toList());
            if (this.policyConditionGroupManager.removeByIds(engineConditionGroupIds)) {
                // 删除条件组条件
                this.policyConditionGroupConditionManager.lambdaUpdate()
                        .in(PolicyConditionGroupCondition::getConditionGroupId, engineConditionGroupIds)
                        .remove();
            }
        }
    }

    /**
     * 获取条件组配置
     *
     * @param ruleId 规则id
     * @return ConditionGroupConfig list
     */
    @Override
    public List<ConditionGroupConfig> getConditionGroupConfig(Integer ruleId) {
        List<PolicyConditionGroup> engineConditionGroups = this.policyConditionGroupManager.lambdaQuery()
                .eq(PolicyConditionGroup::getRuleId, ruleId)
                .orderByAsc(PolicyConditionGroup::getOrderNo)
                .list();
        if (CollUtil.isEmpty(engineConditionGroups)) {
            return Collections.emptyList();
        }
        // 加载所有的用到的条件组条件
        Set<Integer> conditionGroupIds = engineConditionGroups.stream().map(PolicyConditionGroup::getId).collect(Collectors.toSet());
        List<PolicyConditionGroupCondition> policyConditionGroupConditions = this.policyConditionGroupConditionManager.lambdaQuery()
                .in(PolicyConditionGroupCondition::getConditionGroupId, conditionGroupIds)
                .orderByAsc(PolicyConditionGroupCondition::getOrderNo)
                .list();
        Map<Integer, List<PolicyConditionGroupCondition>> conditionGroupConditionMaps;
        Map<Integer, PolicyCondition> conditionMap = Collections.emptyMap();
        Map<Integer, PolicyElement> elementMap = Collections.emptyMap();
        Map<Integer, PolicyVariable> variableMap = Collections.emptyMap();
        if (CollUtil.isNotEmpty(policyConditionGroupConditions)) {
            conditionGroupConditionMaps = policyConditionGroupConditions.stream()
                    .collect(Collectors.groupingBy(PolicyConditionGroupCondition::getConditionGroupId));
            Set<Integer> conditionIds = conditionGroupConditionMaps.values().stream().flatMap(Collection::stream)
                    .map(PolicyConditionGroupCondition::getConditionId)
                    .collect(Collectors.toSet());
            List<PolicyCondition> policyConditions = this.policyConditionManager.lambdaQuery()
                    .in(PolicyCondition::getId, conditionIds).list();
            if (CollUtil.isNotEmpty(policyConditions)) {
                conditionMap = policyConditions.stream().collect(Collectors.toMap(PolicyCondition::getId, Function.identity()));
                elementMap = this.conditionService.getConditionElementMap(conditionMap.values());
                variableMap = this.conditionService.getConditionVariableMap(conditionMap.values());
            }
        } else {
            conditionGroupConditionMaps = Collections.emptyMap();
        }
        // 转换条件组数据
        List<ConditionGroupConfig> conditionGroup = new ArrayList<>();
        for (PolicyConditionGroup engineConditionGroup : engineConditionGroups) {
            ConditionGroupConfig group = new ConditionGroupConfig();
            group.setId(engineConditionGroup.getId());
            group.setName(engineConditionGroup.getName());
            group.setOrderNo(engineConditionGroup.getOrderNo());
            List<PolicyConditionGroupCondition> conditionGroupConditions = conditionGroupConditionMaps.get(engineConditionGroup.getId());
            if (CollUtil.isNotEmpty(conditionGroupConditions)) {
                List<ConditionGroupCondition> groupConditions = new ArrayList<>(conditionGroupConditions.size());
                for (PolicyConditionGroupCondition conditionGroupCondition : conditionGroupConditions) {
                    ConditionGroupCondition conditionSet = new ConditionGroupCondition();
                    conditionSet.setId(conditionGroupCondition.getId());
                    conditionSet.setOrderNo(conditionGroupCondition.getOrderNo());
                    PolicyCondition engineCondition = conditionMap.get(conditionGroupCondition.getConditionId());
                    conditionSet.setCondition(this.conditionService.getConditionResponse(engineCondition, variableMap, elementMap));
                    groupConditions.add(conditionSet);
                }
                group.setConditionGroupCondition(groupConditions);
            }
            conditionGroup.add(group);
        }
        return conditionGroup;
    }

    @Override
    public List<ConditionGroupConfig> pressConditionGroupConfig(List<ConditionGroup> conditionGroups) {
        if (CollUtil.isEmpty(conditionGroups)) {
            return Collections.emptyList();
        }
        List<ConditionGroupConfig> groupArrayList = new ArrayList<>(conditionGroups.size());
        for (ConditionGroup conditionGroup : conditionGroups) {
            ConditionGroupConfig group = new ConditionGroupConfig();
            List<Condition> conditions = conditionGroup.getConditions();
            List<ConditionGroupCondition> conditionGroupConditions = new ArrayList<>(conditions.size());
            for (Condition condition : conditions) {
                ConditionGroupCondition conditionSet = new ConditionGroupCondition();
                ConditionBody conditionResponse = new ConditionBody();
                conditionResponse.setName(condition.getName());
                ConfigBean configBean = new ConfigBean();
                configBean.setLeftValue(this.valueResolve.getConfigValue(condition.getLeftValue()));
                configBean.setSymbol(condition.getOperator().getExplanation());
                configBean.setRightValue(this.valueResolve.getConfigValue(condition.getRightValue()));
                conditionResponse.setConfig(configBean);
                conditionSet.setCondition(conditionResponse);
                conditionGroupConditions.add(conditionSet);
            }
            group.setConditionGroupCondition(conditionGroupConditions);
            groupArrayList.add(group);
        }
        return groupArrayList;
    }

}
