package com.policy.web.vo.decisiontable;

import com.policy.web.vo.condition.ConfigValue;
import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2021/1/1
 * @since 1.0.0
 */
@Data
public class Rows {

    /**
     * 默认值为1
     */
    private Integer priority = 1;

    private List<ConfigValue> conditions = new ArrayList<>();

    @Valid
    private ConfigValue result = new ConfigValue();

}
