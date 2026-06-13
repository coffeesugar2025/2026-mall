package com.policy.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel("返回普通数据")
public class PlainResult<T>
        extends BaseResult {
    private static final long serialVersionUID = 8794822903345524683L;
    @ApiModelProperty("数据")
    private T data;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PlainResult)) return false;
        PlainResult<?> other = (PlainResult) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$data = getData(), other$data = other.getData();
        return !((this$data == null) ? (other$data != null) : !this$data.equals(other$data));
    }

    protected boolean canEqual(Object other) {
        return other instanceof PlainResult;
    }



    public void setData(T data) {
        this.data = data;
    }




    public T getData() {
        return this.data;
    }
}
