package com.policy.core.function;
import com.policy.core.annotation.Function;
import com.policy.core.annotation.Executor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * 返回一个随机UUID字符串。
 *
 * @author
 * @date 2021/2/9
 * @since 1.0.0
 */
@Function
@Slf4j
public class GetUUIDFunction {

    @Executor
    public String executor() {
        return UUID.randomUUID().toString();
    }

}
