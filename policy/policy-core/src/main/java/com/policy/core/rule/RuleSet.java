
package com.policy.core.rule;

import cn.hutool.core.collection.CollUtil;
import com.policy.core.DataSupport;
import com.policy.core.Input;
import com.policy.core.JsonParse;
import com.policy.core.PolicyConfiguration;
import com.policy.core.rule.strategy.RuleSetStrategy;
import com.policy.core.rule.strategy.RuleSetStrategyFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * 引入规则集，改进单规则弊端
 *
 * @author 
 * @create 2020/12/27
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class RuleSet extends DataSupport implements JsonParse {

    /**
     * 规则集
     */
    private List<Rule> rules = new ArrayList<>();

    /**
     * 默认规则
     */
    private Rule defaultRule;

    /**
     * 默认匹配所有的规则结果
     */
    private RuleSetStrategyType strategyType = RuleSetStrategyType.ALL_RULE;


    @JsonIgnore
    private AbnormalAlarm abnormalAlarm = new AbnormalAlarm();


    public void addRule(@NonNull Rule rule) {
        Objects.requireNonNull(rule);
        this.rules.add(rule);
    }

    @Override
    @Nullable
    public Object execute(@NonNull Input input, @NonNull PolicyConfiguration configuration) {
        long startTime = System.currentTimeMillis();
        try {
            RuleSetStrategy ruleSetStrategy = RuleSetStrategyFactory.getInstance(this.strategyType);
            List<Object> actions = ruleSetStrategy.compute(this.rules, input, configuration);
            if (CollUtil.isNotEmpty(actions)) {
                return actions;
            }
            Rule defaultRule = this.getDefaultRule();
            if (Objects.nonNull(defaultRule)) {
                log.debug("结果未命中，存在默认规则，返回默认规则结果");
                return defaultRule.execute(input, configuration);
            }
            log.debug("结果未命中，不存在默认规则，返回:null");
            return null;
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            if (log.isDebugEnabled()) {
                log.debug("引擎计算耗时:{}ms", cost);
            }
            if (cost >= this.getAbnormalAlarm().getTimeOutThreshold()) {
                log.warn("警告：规则集执行超过最大阈值，请检查规则配置，规则Code:" + this.getCode());
            }
        }
    }

    /**
     * 根据rule set json字符串构建一个规则
     *
     * @param jsonString rule json字符串
     * @return rule
     */
    @SneakyThrows
    public static RuleSet buildRuleSet(@NonNull String jsonString) {
        return OBJECT_MAPPER.readValue(jsonString, RuleSet.class);
    }

}
