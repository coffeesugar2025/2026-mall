package com.policy.web.service.decisiontable;

import com.policy.core.decisiontable.DecisionTable;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/27
 * @since 1.0.0
 */
public interface DecisionTablePublishService {

    /**
     * 获取所有的已发布决策表
     *
     * @return list DecisionTable
     */
    List<DecisionTable> getAllPublishDecisionTable();

    /**
     * 根据决策表code，查询发布规则
     *
     * @param workspaceCode 工作空间code
     * @param decisionCode  决策表code
     * @return DecisionTable
     */
    DecisionTable getPublishDecisionTable(String workspaceCode, String decisionCode);

}
