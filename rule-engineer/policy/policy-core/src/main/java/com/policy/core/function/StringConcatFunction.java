package com.policy.core.function;

import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import com.policy.core.annotation.Function;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * <p>
 * 字符串拼接
 *
 * @author
 * @date 2021/2/9
 * @since 1.0.0
 */
@Slf4j
@Function
public class StringConcatFunction {

    @Executor
    public String executor(@Param(value = "source") String source,
                           @Param(value = "target", required = false) String target) {
        return source.concat(target);
    }

}
