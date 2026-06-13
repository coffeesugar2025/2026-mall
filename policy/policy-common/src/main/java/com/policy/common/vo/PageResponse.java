package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel
public class PageResponse extends PageBase {
    private static final long serialVersionUID = -7378163356547441491L;
    @ApiModelProperty("总记录数")
    private Long total;


    public Long getTotal() {
        /* 24 */
        return this.total;
    }

    public PageResponse(long pageIndex, long pageSize, long total) {
        /* 27 */
        setPageIndex(pageIndex);
        /* 28 */
        setPageSize(pageSize);
        /* 29 */
        this.total = Long.valueOf(total);
    }
}
