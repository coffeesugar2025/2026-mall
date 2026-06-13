package com.policy.core.function;

import cn.hutool.core.util.StrUtil;
import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Param;
import com.policy.core.annotation.Function;
import lombok.extern.slf4j.Slf4j;


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
public class ChineseCharacterToPinyinFunction {

    @Executor
    public String executor(@Param(value = "string", required = false) String string,
                           @Param(value = "separator", required = false) String separator) {
        if (StrUtil.isBlank(string)) {
            return string;
        }
        // TODO: 2021/2/9
        return null;
    }

}
