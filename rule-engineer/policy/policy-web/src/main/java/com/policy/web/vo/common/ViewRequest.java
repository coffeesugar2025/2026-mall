package com.policy.web.vo.common;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2021/1/30
 * @since 1.0.0
 */
@Data
public class ViewRequest {

    @NotNull
    private Integer id;

    /**
     * DataStatus
     */
    @NotNull
    private Integer status;

}
