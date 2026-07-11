package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;


@ApiModel
public class Rows<T> {
    @ApiModelProperty("分页数据")
    private List<T> rows;
    @ApiModelProperty("分页参数")
    private PageResponse page;

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public void setPage(PageResponse page) {
        this.page = page;
    }


    protected boolean canEqual(Object other) {
        return other instanceof Rows;
    }



    public String toString() {
        return "Rows(rows=" + getRows() + ", page=" + getPage() + ")";
    }

    public Rows(List<T> rows, PageResponse page) {
        this.rows = rows;
        this.page = page;
    }

    public Rows() {
    }

    public List<T> getRows() {
        return this.rows;
    }

    public PageResponse getPage() {
        return this.page;
    }
}