package com.policy.core.rule.strategy;

import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.rule.Rule;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2020/12/29
 * @since 1.0.0
 */
public interface RuleSetStrategy {

    /**
     * 计算规则结果
     *
     * @param rules         规则集
     * @param input         输入参数
     * @param configuration 配置
     * @return 结果
     */
    List<Object> compute(List<Rule> rules, Input input, PolicyConfiguration configuration);

}
