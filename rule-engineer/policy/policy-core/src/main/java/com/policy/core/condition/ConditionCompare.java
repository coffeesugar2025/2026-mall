package com.policy.core.condition;

import com.policy.core.PolicyConfiguration;
import com.policy.core.Input;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/13
 * @since 1.0.0
 */
public interface ConditionCompare {

    /**
     * 条件比较
     *
     * @param input         入参
     * @param configuration 引擎配置信息
     * @return 比较结果
     */
    boolean compare(Input input, PolicyConfiguration configuration);

}
