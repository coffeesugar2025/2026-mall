package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;


@ApiModel
public class PageBase
        implements Serializable {
    private static final long serialVersionUID = 1L;

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public void setPageIndex(long pageIndex) {
        this.pageIndex = pageIndex;
    }


    @ApiModelProperty("每页数量")
    private long pageSize = 10L;

    public long getPageSize() {
        return this.pageSize;
    }

    @ApiModelProperty("当前页")
    private long pageIndex = 1L;

    public long getPageIndex() {
        return this.pageIndex;
    }
}