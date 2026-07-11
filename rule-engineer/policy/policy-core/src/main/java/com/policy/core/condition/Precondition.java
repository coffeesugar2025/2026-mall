package com.policy.core.condition;

import cn.hutool.core.collection.CollUtil;
import com.policy.core.PolicyConfiguration;
import com.policy.core.Input;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/13
 * @since 1.0.0
 */
@Slf4j
public class Precondition implements ConditionCompare {

    @Getter
    private final List<Condition> precondition = new ArrayList<>();

    /**
     * 比较前提条件
     *
     * @param input         入参
     * @param configuration 规则引擎配置信息
     * @return 比较结果
     */
    @Override
    public boolean compare(Input input, PolicyConfiguration configuration) {
        if (CollUtil.isEmpty(this.precondition)) {
            return true;
        }
        for (Condition condition : this.precondition) {
            if (!condition.compare(input, configuration)) {
                log.debug("前提条件不成立:" + condition.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * 添加前提条件
     *
     * @param condition 条件信息
     */
    public void addPrecondition(@NonNull Condition condition) {
        Objects.requireNonNull(condition);
        Condition.verify(condition);
        this.precondition.add(condition);
    }

}
