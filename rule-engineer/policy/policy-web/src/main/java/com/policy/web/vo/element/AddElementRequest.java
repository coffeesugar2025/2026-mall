package com.policy.web.vo.element;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/7/14
 * @since 1.0.0
 */
@Data
public class AddElementRequest {
    @NotBlank(message = "元素名称不能为空")
    private String name;

    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_&#\\-]*$", message = "元素Code只能字母开头，以及字母数字_&#-组成")
    @NotBlank(message = "元素编码不能为空")
    private String code;

    @NotBlank(message = "元素类型不能为空")
    private String valueType;

    private String description;

}
