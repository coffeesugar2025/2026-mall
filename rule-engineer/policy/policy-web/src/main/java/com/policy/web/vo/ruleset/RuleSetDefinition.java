package com.policy.web.vo.ruleset;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2020/8/28
 * @since 1.0.0
 */
@Data
public class RuleSetDefinition {

    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_&#\\-]*$", message = "规则集Code只能字母开头，以及字母数字_&#-组成")
    private String code;

    private String description;


}
