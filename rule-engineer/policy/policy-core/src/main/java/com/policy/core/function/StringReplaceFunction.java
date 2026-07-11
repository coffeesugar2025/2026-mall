package com.policy.core.function;

import com.policy.core.annotation.Executor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.policy.core.annotation.Function;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2020/11/18
 * @since 1.0.0
 */
@Slf4j
@Function
public class StringReplaceFunction {

    @Executor
    public String executor(@Valid Params params) {
        String value = params.getValue();
        if (value == null) {
            return null;
        }
        String replacement = params.getReplacement();
        return value.replace(params.getTarget(), replacement);
    }

    @Data
    public static class Params {

        private String value;

        @NotNull
        private String target;

        @NotNull
        private String replacement;

    }

}
