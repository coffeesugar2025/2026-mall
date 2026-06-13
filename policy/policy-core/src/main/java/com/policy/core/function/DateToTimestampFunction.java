package com.policy.core.function;

import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Param;

import java.util.Date;
import com.policy.core.annotation.Function;
/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2021/2/24
 * @since 1.0.0
 */
@Function
public class DateToTimestampFunction {

    @Executor
    public Long executor(@Param(value = "date") Date date) {
        return date.getTime();
    }

}
