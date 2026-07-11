package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel("返回分页数据")
public class PageResult<T>
        extends BaseResult {
    private static final long serialVersionUID = -6689010616909835472L;
    @ApiModelProperty("数据")
    private Rows<T> data;


    protected boolean canEqual(Object other) {
        return other instanceof PageResult;
    }


    /* 19 */
    public void setData(Rows<T> data) {
        this.data = data;
    }

    public String toString() {
        return "PageResult(data=" + getData() + ")";
    }

    public PageResult(Rows<T> data) {
        /* 20 */
        this.data = data;
    }


    public PageResult() {
    }


    public Rows<T> getData() {
        /* 28 */
        return this.data;
    }
}