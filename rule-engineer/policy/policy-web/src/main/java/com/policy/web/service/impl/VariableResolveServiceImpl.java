package com.policy.web.service.impl;

import cn.hutool.core.collection.CollUtil;

import com.policy.core.value.Function;
import com.policy.core.value.Value;
import com.policy.core.value.ValueType;
import com.policy.core.value.VariableType;
import com.policy.web.service.ValueResolve;
import com.policy.web.service.VariableResolveService;
import com.policy.web.store.entity.*;

import com.policy.web.store.manager.*;


import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/7/17
 * @since 1.0.0
 */
@Slf4j
@Service
public class VariableResolveServiceImpl implements VariableResolveService {

    @Resource
    private PolicyVariableManager policyVariableManager;
    @Resource
    private PolicyFunctionValueManager policyFunctionValueManager;
    @Resource
    private PolicyFunctionManager policyFunctionManager;
    @Resource
    private ValueResolve valueResolve;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private PolicyElementManager policyElementManager;

    /**
     * 获取所有的变量/函数配置信息
     *
     * @return 变量
     */
    @Override
    public Map<Integer, Value> getAllVariable() {
        log.info("开始加载规则引擎变量");
        Map<Integer, Value> maps = new HashMap<>(100);
        // 查询到所有的变量 待优化
        List<PolicyVariable> engineVariables = this.policyVariableManager.list();
        List<PolicyFunction> engineFunctions = this.policyFunctionManager.list();
        Map<Integer, PolicyFunction> engineFunctionMap = engineFunctions.stream().collect(Collectors.toMap(PolicyFunction::getId, java.util.function.Function.identity()));
        List<PolicyFunctionValue> engineFunctionValues = this.policyFunctionValueManager.list();
        Map<Integer, List<PolicyFunctionValue>> functionValueMap = engineFunctionValues.stream().collect(Collectors.groupingBy(PolicyFunctionValue::getVariableId));
        List<PolicyElement> policyElements = this.policyElementManager.list();
        Map<Integer, PolicyElement> engineElementMap = policyElements.stream().collect(Collectors.toMap(PolicyElement::getId, java.util.function.Function.identity()));
        for (PolicyVariable engineVariable : engineVariables) {
            try {
                Integer type = engineVariable.getType();
                if (VariableType.CONSTANT.getType().equals(type)) {
                    Value value = this.valueResolve.getValue(VariableType.CONSTANT.getType(), engineVariable.getValueType(), engineVariable.getValue(), engineElementMap);
                    maps.put(engineVariable.getId(), value);
                } else if (VariableType.FUNCTION.getType().equals(type)) {
                    maps.put(engineVariable.getId(), this.functionProcess(engineVariable, engineFunctionMap, functionValueMap, engineElementMap));
                }
            } catch (Exception e) {
                log.warn("加载变量失败，变量Id：{}", engineVariable.getId(), e);
            }
        }
        return maps;
    }


    /**
     * 根据变量获取变量/函数配置信息
     *
     * @param id 变量id
     * @return 变量
     */
    @Override
    public Value getVarById(Integer id) {
        PolicyVariable policyVariable = this.policyVariableManager.getById(id);
        if (Objects.equals(policyVariable.getType(), VariableType.CONSTANT.getType())) {
            return this.valueResolve.getValue(VariableType.CONSTANT.getType(), policyVariable.getValueType(), policyVariable.getValue());
        }
        PolicyFunction engineFunction = this.policyFunctionManager.getById(policyVariable.getValue());

        List<PolicyFunctionValue> functionValueList = this.policyFunctionValueManager.lambdaQuery()
                .eq(PolicyFunctionValue::getFunctionId, policyVariable.getValue())
                .eq(PolicyFunctionValue::getVariableId, policyVariable.getId()).list();

        Map<String, Value> param = new HashMap<>(10);
        if (CollUtil.isNotEmpty(functionValueList)) {
            for (PolicyFunctionValue engineFunctionValue : functionValueList) {
                param.put(engineFunctionValue.getParamCode(), this.valueResolve.getValue(engineFunctionValue.getType(), engineFunctionValue.getValueType(), engineFunctionValue.getValue()));
            }
        }
        Object abstractFunction = this.applicationContext.getBean(engineFunction.getExecutor());
        return new Function(engineFunction.getId(), abstractFunction, ValueType.getByValue(engineFunction.getReturnValueType()), param);

    }


    /**
     * 规则引擎函数处理
     *
     * @param policyVariable 规则函数元数据
     * @param engineFunctionMap  函数缓存数据
     * @return Function
     */
    @Override
    public Function functionProcess(PolicyVariable policyVariable, Map<Integer, PolicyFunction> engineFunctionMap, Map<Integer, List<PolicyFunctionValue>> functionValueMap, Map<Integer, PolicyElement> engineElementMap) {
        Integer functionId = Integer.valueOf(policyVariable.getValue());
        PolicyFunction engineFunction = engineFunctionMap.get(functionId);

        List<PolicyFunctionValue> functionValueList = functionValueMap.get(policyVariable.getId());
        Map<String, Value> param = new HashMap<>(10);
        if (CollUtil.isNotEmpty(functionValueList)) {
            for (PolicyFunctionValue engineFunctionValue : functionValueList) {
                param.put(engineFunctionValue.getParamCode(), this.valueResolve.getValue(engineFunctionValue.getType(), engineFunctionValue.getValueType(), engineFunctionValue.getValue(), engineElementMap));
            }
        }
        Object abstractFunction = this.applicationContext.getBean(engineFunction.getExecutor());
        return new Function(engineFunction.getId(), abstractFunction, ValueType.getByValue(engineFunction.getReturnValueType()), param);
    }

}
