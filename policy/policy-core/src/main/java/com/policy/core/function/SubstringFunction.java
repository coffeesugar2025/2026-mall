package com.policy.core.function;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.policy.core.annotation.Executor;
import com.policy.core.annotation.FailureStrategy;
import com.policy.core.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import com.policy.core.annotation.Function;
/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2021/2/9
 * @since 1.0.0
 */
@Slf4j
@Function
public class SubstringFunction {

    @Executor(failureFor = StringIndexOutOfBoundsException.class)
    public String executor(@Param(value = "string", required = false) String string,
                           @Param(value = "beginIndex") Integer beginIndex,
                           @Param(value = "endIndex", required = false) Integer endIndex) {
        if (StrUtil.isBlank(string)) {
            return string;
        }
        if (Validator.isEmpty(endIndex)) {
            return string.substring(beginIndex);
        }
        return string.substring(beginIndex, endIndex);
    }

    @FailureStrategy
    public String failureStrategy(@Param(value = "string", required = false) String string,
                                  @Param(value = "beginIndex") Integer beginIndex,
                                  @Param(value = "endIndex", required = false) Integer endIndex) {
        log.error("字符串索引超出范围异常：{},{},{}", string, beginIndex, endIndex);
        return string;
    }

}
