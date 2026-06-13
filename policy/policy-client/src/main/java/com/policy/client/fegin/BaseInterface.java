package com.policy.client.fegin;

import com.policy.client.param.BatchParam;
import com.policy.client.param.ExecuteParam;
import com.policy.client.param.IsExistsParam;
import com.policy.client.result.BatchExecuteResult;
import com.policy.client.result.ExecuteResult;
import com.policy.client.result.IsExistsResult;
import feign.Headers;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2020-12-29 15:32
 * @since 1.0.0
 */
@Headers({"Content-Type: application/json", "Accept: application/json"})
public interface BaseInterface {

    /**
     * 调用规则引擎中的规则
     *
     * @param executeParam 入参
     * @return ExecuteRuleResult
     */
    ExecuteResult execute(ExecuteParam executeParam);

    /**
     * 引擎中是否存在此规则
     *
     * @param existsParam 入参
     * @return IsExistsResult
     */
    IsExistsResult isExists(IsExistsParam existsParam);


    /**
     * 批量执行规则
     *
     * @param batchParam 批量参数
     * @return list
     */
    BatchExecuteResult batchExecute(BatchParam batchParam);

}
