package com.policy.web.service.ruleset.impl;

import com.policy.core.DefaultInput;
import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.RuleSetEngine;
import com.policy.common.exception.ValidException;
import com.policy.core.rule.RuleSet;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.RunTestService;
import com.policy.web.store.entity.PolicyRuleSetPublish;
import com.policy.web.store.manager.PolicyRuleSetPublishManager;
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
 * @date 2021/1/15
 * @since 1.0.0
 */
@Slf4j
@Service
public class RuleSetRunTestServiceImpl implements RunTestService {

    @Resource
    private PolicyConfiguration PolicyConfiguration;
    @Resource
    private PolicyRuleSetPublishManager policyRuleSetPublishManager;

    /**
     * 规则集模拟运行
     *
     * @param runTestRequest 规则集参数信息
     * @return result
     */
    @Override
    public Object run(RunTestRequest runTestRequest) {
        log.info("模拟运行规则集：{}", runTestRequest.getCode());
        PolicyRuleSetPublish ruleSetPublish = this.policyRuleSetPublishManager.lambdaQuery()
                .eq(PolicyRuleSetPublish::getStatus, runTestRequest.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetCode, runTestRequest.getCode())
                .eq(PolicyRuleSetPublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                .one();
        if (ruleSetPublish == null) {
            // 如果待发布找不到，用已发布  此场景出现在只有一个已发布的时候
            ruleSetPublish = this.policyRuleSetPublishManager.lambdaQuery()
                    .eq(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyRuleSetPublish::getRuleSetCode, runTestRequest.getCode())
                    .eq(PolicyRuleSetPublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                    .one();
            if (ruleSetPublish == null) {
                throw new ValidException("找不到可运行的规则集数据:{},{},{}", runTestRequest.getWorkspaceCode(), runTestRequest.getCode(), runTestRequest.getStatus());
            }
        }
        Input input = new DefaultInput();
        Map<String, Object> params = runTestRequest.getParam();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            input.put(param.getKey(), param.getValue());
        }
        log.info("初始化规则集引擎");
        PolicyConfiguration PolicyConfiguration = new PolicyConfiguration();
        RuleSetEngine engine = new RuleSetEngine(PolicyConfiguration);
        RuleSet rule = RuleSet.buildRuleSet(ruleSetPublish.getData());
        engine.add(rule);
        // 加载变量
        engine.getConfiguration().setEngineVariable(this.PolicyConfiguration.getEngineVariable());
        return engine.execute(input, runTestRequest.getWorkspaceCode(), runTestRequest.getCode());
    }

}
