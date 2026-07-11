package com.policy.web.service;

import com.policy.core.value.Function;
import com.policy.core.value.Value;
import com.policy.web.store.entity.PolicyElement;
import com.policy.web.store.entity.PolicyFunction;
import com.policy.web.store.entity.PolicyFunctionValue;
import com.policy.web.store.entity.PolicyVariable;

import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2020/7/17
 * @since 1.0.0
 */
public interface VariableResolveService {

    /**
     * 获取所有的变量/函数配置信息
     *
     * @return 变量
     */
    Map<Integer, Value> getAllVariable();

    /**
     * 根据变量获取变量/函数配置信息
     *
     * @param id 变量id
     * @return 变量
     */
    Value getVarById(Integer id);

    /**
     * 规则引擎函数处理
     *
     * @param policyVariable 规则函数元数据
     * @param engineFunctionMap  engineFunction缓存数据
     * @return Function
     */
    Function functionProcess(PolicyVariable policyVariable, Map<Integer, PolicyFunction> engineFunctionMap, Map<Integer, List<PolicyFunctionValue>> functionValueMap, Map<Integer, PolicyElement> engineElementMap);

}
