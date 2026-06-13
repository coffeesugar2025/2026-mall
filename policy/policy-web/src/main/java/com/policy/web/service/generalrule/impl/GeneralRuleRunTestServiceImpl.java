package com.policy.web.service.generalrule.impl;

import com.policy.core.DefaultInput;
import com.policy.core.GeneralPolicy;
import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.common.exception.ValidException;
import com.policy.core.rule.GeneralRule;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.RunTestService;
import com.policy.web.store.entity.PolicyGeneralRulePublish;
import com.policy.web.store.manager.PolicyGeneralRulePublishManager;
import com.policy.web.vo.generalrule.RunTestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
@Primary
@Slf4j
@Service
public class GeneralRuleRunTestServiceImpl implements RunTestService {

    @Resource
    private PolicyConfiguration PolicyConfiguration;
    @Resource
    private PolicyGeneralRulePublishManager policyGeneralRulePublishManager;

    /**
     * 规则模拟运行
     *
     * @param runTestRequest 规则参数信息
     * @return result
     */
    @Override
    public Object run(RunTestRequest runTestRequest) {
        log.info("模拟运行规则：{}", runTestRequest.getCode());
        PolicyGeneralRulePublish rulePublish = this.policyGeneralRulePublishManager.lambdaQuery()
                .eq(PolicyGeneralRulePublish::getStatus, runTestRequest.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleCode, runTestRequest.getCode())
                .eq(PolicyGeneralRulePublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                .one();
        if (rulePublish == null) {
            // 如果待发布找不到，用已发布  此场景出现在只有一个已发布的时候
            rulePublish = this.policyGeneralRulePublishManager.lambdaQuery()
                    .eq(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyGeneralRulePublish::getGeneralRuleCode, runTestRequest.getCode())
                    .eq(PolicyGeneralRulePublish::getWorkspaceCode, runTestRequest.getWorkspaceCode())
                    .one();
            if (rulePublish == null) {
                throw new ValidException("找不到可运行的规则数据:{},{},{}", runTestRequest.getWorkspaceCode(), runTestRequest.getCode(), runTestRequest.getStatus());
            }
        }
        Input input = new DefaultInput();
        Map<String, Object> params = runTestRequest.getParam();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            input.put(param.getKey(), param.getValue());
        }
        log.info("初始化规则引擎");
        PolicyConfiguration PolicyConfiguration = new PolicyConfiguration();
        GeneralPolicy engine = new GeneralPolicy(PolicyConfiguration);
        GeneralRule rule = GeneralRule.buildRule(rulePublish.getData());
        engine.add(rule);
        // 加载变量
        engine.getConfiguration().setEngineVariable(this.PolicyConfiguration.getEngineVariable());
        return engine.execute(input, runTestRequest.getWorkspaceCode(), runTestRequest.getCode());
    }

}
