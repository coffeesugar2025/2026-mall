package com.policy.web.vo.element;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2020/8/25
 * @since 1.0.0
 */
@Data
public class UpdateElementRequest {

    @NotNull
    private Integer id;

    @NotBlank(message = "元素名称不能为空")
    private String name;

    private String description;

}
