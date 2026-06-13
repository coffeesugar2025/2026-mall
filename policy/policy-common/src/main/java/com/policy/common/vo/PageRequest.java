package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;


@ApiModel
public class PageRequest<T> {

    @ApiModelProperty("查询条件")
    private T query;


    public void setQuery(T query) {
        /* 19 */
        this.query = query;
    }

    public void setPage(PageBase page) {
        this.page = page;
    }

    public void setOrders(List<OrderBy> orders) {
        this.orders = orders;
    }


    protected boolean canEqual(Object other) {
        return other instanceof PageRequest;
    }


    public T getQuery() {
        /* 22 */
        return this.query;

    }

    @ApiModelProperty("分页参数")
    /* 24 */ private PageBase page = new PageBase();

    public PageBase getPage() {
        /* 25 */
        return this.page;

    }

    @ApiModelProperty("排序条件")
    /* 27 */ private List<OrderBy> orders = new ArrayList<>();

    /* 28 */
    public List<OrderBy> getOrders() {
        return this.orders;
    }

    @ApiModel
    public static class OrderBy {

        @ApiModelProperty("排序列名")
        /* 31 */ private String columnName;

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        @ApiModelProperty("是否降序")
        private boolean desc;

        public void setDesc(boolean desc) {
            this.desc = desc;
        }


        public String getColumnName() {
            /* 34 */
            return this.columnName;

        }

        public boolean isDesc() {
            /* 36 */
            return this.desc;

        }

    }

}