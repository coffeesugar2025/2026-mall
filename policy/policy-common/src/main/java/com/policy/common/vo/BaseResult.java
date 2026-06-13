package com.policy.common.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
 import io.swagger.annotations.ApiModel;
 import io.swagger.annotations.ApiModelProperty;
 import java.io.Serializable;
 import java.util.Date;


@ApiModel
public class BaseResult
        implements Serializable {
    private static final long serialVersionUID = 1L;


    public void setState(ResultState state) {
        /* 20 */
        this.state = state;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @JsonFormat(timezone = "yyyy-MM-dd HH:mm:ss")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }



    protected boolean canEqual(Object other) {
        return other instanceof BaseResult;
    }


    public String toString() {
        return "BaseResult(state=" + getState() + ", message=" + getMessage() + ", code=" + getCode() + ", timestamp=" + getTimestamp() + ")";
    }


    public ResultState getState() {
        /* 25 */
        return this.state;
        
    }

    
    public String getMessage() {
        /* 28 */
        return this.message;
        
    }

    
    public Integer getCode() {
        /* 31 */
        return this.code;
        
    }

    
    
    public Date getTimestamp() {
        /* 35 */
        return this.timestamp;
        
    }

    
    @ApiModelProperty("返回状态")
    /* 38 */ private ResultState state = ResultState.SUCCESS;
    @ApiModelProperty("返回消息")
    /* 39 */ private String message = "执行成功";
    @ApiModelProperty("返回码")
    /* 40 */ private Integer code = Integer.valueOf(200);
    @ApiModelProperty("时间轴")
    @JsonFormat(timezone = "yyyy-MM-dd HH:mm:ss")
    /* 41 */ private Date timestamp = new Date();

    
    
    
    public static BaseResult ok() {
        /* 45 */
        return new BaseResult();
        
    }

    
    
    public static BaseResult err() {
        /* 49 */
        return err(Integer.valueOf(400), "执行失败");
        
    }

    
    
    public static BaseResult err(Integer code, String message) {
        /* 53 */
        BaseResult baseResult = new BaseResult();
        /* 54 */
        baseResult.setState(ResultState.ERROR);
        /* 55 */
        baseResult.setMessage(message);
        /* 56 */
        baseResult.setCode(code);
        /* 57 */
        baseResult.setTimestamp(new Date());
        /* 58 */
        return baseResult;
        
    }
    
}
