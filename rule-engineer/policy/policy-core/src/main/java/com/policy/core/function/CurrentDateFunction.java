package com.policy.core.function;

import com.policy.core.annotation.Executor;

import java.util.*;
import com.policy.core.annotation.Function;
/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/11/19
 * @since 1.0.0
 */
@Function
public class CurrentDateFunction {

    @Executor
    public Date executor() {
        return new Date();
    }

}
