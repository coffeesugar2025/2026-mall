package com.policy.core.rule.strategy;


import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.rule.Rule;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/29
 * @since 1.0.0
 */
@Slf4j
public class AllRuleStrategy implements RuleSetStrategy {

    private static final AllRuleStrategy ALL_RULE_STRATEGY = new AllRuleStrategy();

    private AllRuleStrategy() {
    }

    public static AllRuleStrategy getInstance() {
        return ALL_RULE_STRATEGY;
    }

    @Override
    public List<Object> compute(List<Rule> rules, Input input, PolicyConfiguration configuration) {
        List<Object> actions = new ArrayList<>();
        for (Rule rule : rules) {
            log.debug("执行规则：" + rule.getName());
            Object action = rule.execute(input, configuration);
            if (action != null) {
                if (log.isDebugEnabled()) {
                    log.debug("规则：{} 命中结果：{}", rule.getName(), action);
                }
                actions.add(action);
            }
        }
        return actions;
    }
}
