package com.policy.core.function;

import cn.hutool.core.date.DateUtil;
import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Function;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2021/2/24
 * @since 1.0.0
 */
@Function(name = "获取今天星期几")
public class GetTheDayOfTheWeekFunction {

    @Executor
    public String executor() {
        return DateUtil.thisDayOfWeekEnum().toChinese();
    }

}
