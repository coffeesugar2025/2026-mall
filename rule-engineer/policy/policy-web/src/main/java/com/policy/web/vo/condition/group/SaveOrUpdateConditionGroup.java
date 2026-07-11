package com.policy.web.vo.condition.group;


import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/28
 * @since 1.0.0
 */
@Data
public class SaveOrUpdateConditionGroup {

    private Integer id;
    private String name;
    @NotNull
    private Integer ruleId;
    private Integer orderNo;

}
