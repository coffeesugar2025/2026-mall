package com.policy.web.service.decisiontable.impl;

import com.policy.core.DecisionTableEngine;
import com.policy.core.DefaultInput;
import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.decisiontable.DecisionTable;
import com.policy.common.exception.ValidException;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.RunTestService;
import com.policy.web.store.entity.PolicyDecisionTablePublish;
import com.policy.web.store.manager.PolicyDecisionTablePublishManager;
import com.policy.web.vo.generalrule.RunTestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/26
 * @since 1.0.0
 */
@Slf4j
@Service
public class DecisionTableRunTestServiceImpl implements RunTestService {

    @Resource
    private PolicyConfiguration PolicyConfiguration;
    @Resource
    private PolicyDecisionTablePublishManager policyDecisionTablePublishManager;

    /**
     * 规则模拟运行
     *
     * @param runTestRequest 规则参数信息
     * @return result
     */
    @Override
    public Object run(RunTestRequest runTestRequest) {
        log.info("模拟运行决策表：{}", runTestRequest.getCode());
        PolicyDecisionTablePublish rulePublish = this.policyDecisionTablePublishManager.lambdaQuery()
                .eq(PolicyDecisionTablePublish::getStatus, runTestRequest.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableCode, runTestRequest.getCode())
                .eq(PolicyDecisionTablePublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                .one();
        if (rulePublish == null) {
            // 如果待发布找不到，用已发布  此场景出现在只有一个已发布的时候
            rulePublish = this.policyDecisionTablePublishManager.lambdaQuery()
                    .eq(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyDecisionTablePublish::getDecisionTableCode, runTestRequest.getCode())
                    .eq(PolicyDecisionTablePublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                    .one();
            if (rulePublish == null) {
                throw new ValidException("找不到可运行的决策表数据:{},{},{}", runTestRequest.getWorkspaceCode(), runTestRequest.getCode(), runTestRequest.getStatus());
            }
        }
        Input input = new DefaultInput();
        Map<String, Object> params = runTestRequest.getParam();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            input.put(param.getKey(), param.getValue());
        }
        log.info("初始化决策表引擎");
        PolicyConfiguration PolicyConfiguration = new PolicyConfiguration();
        DecisionTableEngine engine = new DecisionTableEngine(PolicyConfiguration);
        DecisionTable decisionTable = DecisionTable.buildDecisionTable(rulePublish.getData());
        engine.add(decisionTable);
        // 加载变量
        engine.getConfiguration().setEngineVariable(this.PolicyConfiguration.getEngineVariable());
        return engine.execute(input, runTestRequest.getWorkspaceCode(), runTestRequest.getCode());
    }

}
