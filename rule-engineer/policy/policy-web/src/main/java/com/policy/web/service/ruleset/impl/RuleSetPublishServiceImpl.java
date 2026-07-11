package com.policy.web.service.ruleset.impl;

import cn.hutool.core.collection.CollUtil;
import com.policy.core.rule.RuleSet;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.ruleset.RuleSetPublishService;
import com.policy.web.store.entity.PolicyRuleSetPublish;
import com.policy.web.store.manager.PolicyRuleSetPublishManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2021/1/15
 * @since 1.0.0
 */
@Slf4j
@Service
public class RuleSetPublishServiceImpl implements RuleSetPublishService {

    @Resource
    private PolicyRuleSetPublishManager policyRuleSetPublishManager;

    /**
     * 获取所有的已发布规则集
     *
     * @return list DecisionTable
     */
    @Override
    public List<RuleSet> getAllPublishRuleSet() {
        List<PolicyRuleSetPublish> ruleSetPublishes = this.policyRuleSetPublishManager.lambdaQuery()
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .list();
        if (CollUtil.isEmpty(ruleSetPublishes)) {
            return Collections.emptyList();
        }
        List<RuleSet> ruleSets = new ArrayList<>(ruleSetPublishes.size());
        for (PolicyRuleSetPublish publish : ruleSetPublishes) {
            try {
                log.info("parse rule set for workspace code: {} rule code: {}", publish.getWorkspaceCode(), publish.getRuleSetCode());
                RuleSet ruleSet = RuleSet.buildRuleSet(publish.getData());
                ruleSets.add(ruleSet);
            } catch (Exception e) {
                log.error("parse rule set error ", e);
            }
        }
        return ruleSets;
    }

    /**
     * 根据规则集code，查询发布规则
     *
     * @param workspaceCode 工作空间code
     * @param code          规则集code
     * @return DecisionTable
     */
    @Override
    public RuleSet getPublishRuleSet(String workspaceCode, String code) {
        PolicyRuleSetPublish ruleSetPublish = this.policyRuleSetPublishManager.lambdaQuery()
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetCode, code)
                .eq(PolicyRuleSetPublish::getWorkspaceCode, workspaceCode)
                .one();
        return RuleSet.buildRuleSet(ruleSetPublish.getData());
    }
}
