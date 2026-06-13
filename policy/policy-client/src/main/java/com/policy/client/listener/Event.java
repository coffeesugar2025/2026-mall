package com.policy.client.listener;

import com.policy.client.result.Output;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2021/2/1
 * @since 1.0.0
 */
public interface Event {

    /**
     * 当监听的规则code 有输出时触发
     *
     * @param code   规则/决策表/规则集 code
     * @param output 监听结果
     */
    void onOut(String code, Output output);

}
