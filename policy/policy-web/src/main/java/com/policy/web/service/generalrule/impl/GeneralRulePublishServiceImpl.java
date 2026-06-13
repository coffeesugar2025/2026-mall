package com.policy.web.service.generalrule.impl;

import cn.hutool.core.collection.CollUtil;
import com.policy.common.enums.DataStatus;
import com.policy.core.rule.GeneralRule;
import com.policy.web.service.generalrule.GeneralRulePublishService;
import com.policy.web.store.entity.PolicyGeneralRulePublish;
import com.policy.web.store.manager.PolicyGeneralRulePublishManager;
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
 * @date 2020/9/4
 * @since 1.0.0
 */
@Slf4j
@Service
public class GeneralRulePublishServiceImpl implements GeneralRulePublishService {

    @Resource
    private PolicyGeneralRulePublishManager policyGeneralRulePublishManager;

    /**
     * 根据规则code，查询发布规则
     *
     * @param workspaceCode 工作空间code
     * @param ruleCode      规则code
     * @return 规则
     */
    @Override
    public GeneralRule getPublishGeneralRule(String workspaceCode, String ruleCode) {
        PolicyGeneralRulePublish rulePublish = this.policyGeneralRulePublishManager.lambdaQuery()
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleCode, ruleCode)
                .eq(PolicyGeneralRulePublish::getWorkspaceCode, workspaceCode)
                .one();
        return GeneralRule.buildRule(rulePublish.getData());
    }

    /**
     * 获取所有的已发布规则
     *
     * @return rule
     */
    @Override
    public List<GeneralRule> getAllPublishGeneralRule() {
        List<PolicyGeneralRulePublish> rulePublishList = this.policyGeneralRulePublishManager.lambdaQuery()
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .list();
        if (CollUtil.isEmpty(rulePublishList)) {
            return Collections.emptyList();
        }
        List<GeneralRule> rules = new ArrayList<>(rulePublishList.size());
        for (PolicyGeneralRulePublish publish : rulePublishList) {
            try {
                log.info("parse rule for workspace code: {} rule code: {}", publish.getWorkspaceCode(), publish.getGeneralRuleCode());
                GeneralRule rule = GeneralRule.buildRule(publish.getData());
                rules.add(rule);
            } catch (Exception e) {
                log.error("parse rule error ", e);
            }
        }
        return rules;
    }

}
