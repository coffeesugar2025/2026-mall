package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


@ApiModel("Id查询请求")
public class IdRequest {
    @ApiModelProperty("Id")
    @NotNull(message = "Id不能为空")
    private Integer id;

    public void setId(Integer id) {
        this.id = id;
    }


    public String toString() {
        return "IdRequest(id=" + getId() + ")";
    }

    public IdRequest(Integer id) {
        this.id = id;
    }


    public IdRequest() {
    }

    public Integer getId() {
        return this.id;
    }
}