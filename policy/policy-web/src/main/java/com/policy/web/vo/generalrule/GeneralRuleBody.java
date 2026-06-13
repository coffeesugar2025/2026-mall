package com.policy.web.vo.generalrule;

import com.policy.web.vo.condition.ConditionGroupConfig;
import com.policy.web.vo.condition.ConfigValue;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2021/1/23
 * @since 1.0.0
 */
@Data
public class GeneralRuleBody {

    @NotNull
    private Integer id;

    private List<ConditionGroupConfig> conditionGroup;

    @NotNull
    private ConfigValue action;

    private DefaultAction defaultAction;

}
