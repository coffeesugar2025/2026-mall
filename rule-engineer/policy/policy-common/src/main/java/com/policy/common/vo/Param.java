package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;


@ApiModel("公用参数")
public class Param<T> {

    @ApiModelProperty("公用参数字段")
    @NotNull(message = "参数不能为空")
    private T param;


    public void setParam(T param) {
        /* 19 */
        this.param = param;
    }


    public Param(T param) {

        this.param = param;

    }


    public Param() {
    }


    public T getParam() {

        return this.param;

    }

}

