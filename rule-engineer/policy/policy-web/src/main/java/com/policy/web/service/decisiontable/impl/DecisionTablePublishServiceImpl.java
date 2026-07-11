package com.policy.web.service.decisiontable.impl;

import cn.hutool.core.collection.CollUtil;
import com.policy.core.decisiontable.DecisionTable;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.decisiontable.DecisionTablePublishService;
import com.policy.web.store.entity.PolicyDecisionTablePublish;
import com.policy.web.store.manager.PolicyDecisionTablePublishManager;
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
 * @create 2020/12/27
 * @since 1.0.0
 */
@Slf4j
@Service
public class DecisionTablePublishServiceImpl implements DecisionTablePublishService {

    @Resource
    private PolicyDecisionTablePublishManager policyDecisionTablePublishManager;

    @Override
    public List<DecisionTable> getAllPublishDecisionTable() {
        List<PolicyDecisionTablePublish> decisionTablePublishes = this.policyDecisionTablePublishManager.lambdaQuery()
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .list();
        if (CollUtil.isEmpty(decisionTablePublishes)) {
            return Collections.emptyList();
        }
        List<DecisionTable> decisionTables = new ArrayList<>(decisionTablePublishes.size());
        for (PolicyDecisionTablePublish publish : decisionTablePublishes) {
            try {
                log.info("parse decisionTable for workspace code: {} decisionTable code: {}", publish.getWorkspaceCode(), publish.getDecisionTableCode());
                DecisionTable decisionTable = DecisionTable.buildDecisionTable(publish.getData());
                decisionTables.add(decisionTable);
            } catch (Exception e) {
                log.error("parse decisionTable error ", e);
            }
        }
        return decisionTables;
    }

    @Override
    public DecisionTable getPublishDecisionTable(String workspaceCode, String decisionCode) {
        PolicyDecisionTablePublish rulePublish = this.policyDecisionTablePublishManager.lambdaQuery()
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableCode, decisionCode)
                .eq(PolicyDecisionTablePublish::getWorkspaceCode, workspaceCode)
                .one();
        return DecisionTable.buildDecisionTable(rulePublish.getData());
    }

}
