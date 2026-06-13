package com.policy.core.function;

import cn.hutool.core.collection.CollUtil;
import com.policy.core.annotation.Executor;
import com.policy.core.annotation.Param;
import lombok.extern.slf4j.Slf4j;
import com.policy.core.annotation.Function;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 * 集合去除重复数据
 *
 * @author 
 * @create 2020/11/18
 * @since 1.0.0
 */
@Slf4j
@Function
public class CollectionDeduplicationFunction<T> {

    @Executor
    public List<T> executor(@Param(value = "list", required = false) List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().distinct().collect(Collectors.toList());
    }

}
