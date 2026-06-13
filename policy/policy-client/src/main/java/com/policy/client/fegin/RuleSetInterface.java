package com.policy.client.fegin;

import com.policy.client.param.BatchParam;
import com.policy.client.param.ExecuteParam;
import com.policy.client.param.IsExistsParam;
import com.policy.client.result.BatchExecuteResult;
import com.policy.client.result.ExecuteResult;
import com.policy.client.result.IsExistsResult;
import feign.RequestLine;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020-12-29 15:37
 * @since 1.0.0
 */
public interface RuleSetInterface extends BaseInterface {

    /**
     * 调用规则引擎中的规则集
     *
     * @param executeParam 入参
     * @return ExecuteRuleResult
     */
    @Override
    @RequestLine("POST /policy/ruleSet/execute")
    ExecuteResult execute(ExecuteParam executeParam);

    /**
     * 引擎中是否存在此规则集
     *
     * @param existsParam 入参
     * @return IsExistsResult
     */
    @Override
    @RequestLine("POST /policy/ruleSet/isExists")
    IsExistsResult isExists(IsExistsParam existsParam);


    /**
     * 批量执行规则集
     *
     * @param batchParam 批量参数
     * @return list
     */
    @Override
    @RequestLine("POST /policy/ruleSet/batchExecute")
    BatchExecuteResult batchExecute(BatchParam batchParam);

}
