package com.policy.core.function;

import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Function;
import com.policy.core.annotation.Param;
/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * 字母转大写
 *
 * @author
 * @create 2020/12/24
 * @since 1.0.0
 */
@Function
public class LetterToUpperCaseFunction {

    @Executor
    public String executor(@Param(value = "letter",required = false) String letter) {
        if (letter == null) {
            return null;
        }
        return letter.toUpperCase();
    }

}
