package com.policy.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.policy.common.exception.ValidException;
import com.policy.common.vo.Rows;
import com.policy.core.value.Constant;
import com.policy.core.value.Function;
import com.policy.core.value.ValueType;
import com.policy.web.service.FunctionService;
import com.policy.web.store.entity.PolicyFunction;
import com.policy.web.store.entity.PolicyFunctionParam;
import com.policy.web.store.manager.PolicyFunctionManager;
import com.policy.web.store.manager.PolicyFunctionParamManager;
import com.policy.common.util.PageUtils;
import com.policy.web.vo.common.ExecuteTestRequest;
import com.policy.web.vo.function.FunctionParam;
import com.policy.web.vo.function.GetFunctionResponse;
import com.policy.web.vo.function.ListFunctionRequest;
import com.policy.web.vo.function.ListFunctionResponse;
import com.policy.web.vo.variable.ParamValue;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.policy.common.vo.PageResult;
import com.policy.common.vo.PageRequest;
import com.policy.common.vo.PageBase;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/27
 * @since 1.0.0
 */
@Service
public class FunctionServiceImpl implements FunctionService {

    @Resource
    private PolicyFunctionManager policyFunctionManager;
    @Resource
    private PolicyFunctionParamManager policyFunctionParamManager;
    @Resource
    private ApplicationContext applicationContext;

    private static final String FUNCTION_PACKAGE = "com.policy.web.function";

    /**
     * 函数列表
     *
     * @param pageRequest param
     * @return list
     */
    @Override
    public PageResult<ListFunctionResponse> list(PageRequest<ListFunctionRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();

        QueryWrapper<PolicyFunction> wrapper = new QueryWrapper<>();
        PageUtils.defaultOrder(orders, wrapper);
        ListFunctionRequest query = pageRequest.getQuery();
        if (Validator.isNotEmpty(query.getName())) {
            wrapper.lambda().like(PolicyFunction::getName, query.getName());
        }
        IPage<PolicyFunction> functionPage = policyFunctionManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), wrapper);

        List<PolicyFunction> records = functionPage.getRecords();

        // 获取本次请求用到的所有函数参数
        Map<Integer, List<PolicyFunctionParam>> functionParamMap = Optional.of(records)
                .filter(CollUtil::isNotEmpty)
                .map(m -> {
                    List<Integer> functionIds = m.stream().map(PolicyFunction::getId).collect(Collectors.toList());
                    return this.policyFunctionParamManager.lambdaQuery().in(PolicyFunctionParam::getFunctionId, functionIds).list();
                })
                .filter(CollUtil::isNotEmpty)
                .map(m -> m.stream().collect(Collectors.groupingBy(PolicyFunctionParam::getFunctionId))).orElse(Collections.emptyMap());

        PageResult<ListFunctionResponse> pageResult = new PageResult<>();
        List<ListFunctionResponse> responseList = records.stream().map(m -> {
            ListFunctionResponse functionResponse = new ListFunctionResponse();
            functionResponse.setId(m.getId());
            functionResponse.setName(m.getName());
            functionResponse.setExecutor(m.getExecutor());
            functionResponse.setReturnValueType(m.getReturnValueType());
            functionResponse.setCreateTime(m.getCreateTime());
            // 处理方法参数
            functionResponse.setParams(this.getFunctionParam(functionParamMap.get(m.getId())));
            return functionResponse;
        }).collect(Collectors.toList());
        pageResult.setData(new Rows<>(responseList, PageUtils.getPageResponse(functionPage)));
        return pageResult;
    }

    /**
     * 查询函数详情
     *
     * @param id 函数id
     * @return 函数信息
     */
    @Override
    public GetFunctionResponse get(Integer id) {
        PolicyFunction policyFunction = this.policyFunctionManager.getById(id);
        if (policyFunction == null) {
            throw new ValidException("不存在函数：{}", id);
        }
        GetFunctionResponse functionResponse = new GetFunctionResponse();
        functionResponse.setId(policyFunction.getId());
        functionResponse.setName(policyFunction.getName());
        functionResponse.setDescription(policyFunction.getDescription());
        functionResponse.setExecutor(policyFunction.getExecutor());
        functionResponse.setReturnValueType(policyFunction.getReturnValueType());
        // 处理方法参数
        List<PolicyFunctionParam> functionParamList = this.policyFunctionParamManager.lambdaQuery().eq(PolicyFunctionParam::getFunctionId, id).list();
        functionResponse.setParams(this.getFunctionParam(functionParamList));
        return functionResponse;
    }


    private void saveFunctionParam(List<FunctionParam> param, Integer functionId) {
        if (CollUtil.isNotEmpty(param)) {
            List<PolicyFunctionParam> functionParamList = param.stream().map(m -> {
                PolicyFunctionParam policyFunctionParam = new PolicyFunctionParam();
                policyFunctionParam.setFunctionId(functionId);
                policyFunctionParam.setParamName(m.getName());
                policyFunctionParam.setParamCode(m.getCode());
                policyFunctionParam.setValueType(m.getValueType());
                return policyFunctionParam;
            }).collect(Collectors.toList());
            this.policyFunctionParamManager.saveBatch(functionParamList);
        }
    }

    /**
     * 函数模拟测试
     *
     * @param executeTestRequest 函数入参值
     * @return result
     */
    @Override
    public Object run(ExecuteTestRequest executeTestRequest) {
        PolicyFunction engineFunction = this.policyFunctionManager.getById(executeTestRequest.getId());
        if (engineFunction == null) {
            throw new ValidException("不存在函数：{}", executeTestRequest.getId());
        }
        String executor = engineFunction.getExecutor();
        if (this.applicationContext.containsBean(executor)) {
            Object abstractFunction = this.applicationContext.getBean(executor);
            // 执行函数入参
            Map<String, Object> paramValue = this.getParamValue(executeTestRequest.getParamValues());
            Function function = new Function(abstractFunction, ValueType.getByValue(engineFunction.getReturnValueType()));
            return function.executor(paramValue);
        } else {
            throw new ValidException("容器中找不到{}函数", executor);
        }
    }

    /**
     * 处理函数值
     *
     * @param paramValue 函数参数值
     * @return map
     */
    private Map<String, Object> getParamValue(List<ParamValue> paramValue) {
        Map<String, Object> paramMap = new HashMap<>(paramValue.size());
        for (ParamValue value : paramValue) {
            Constant constant = new Constant(value.getValue(), ValueType.getByValue(value.getValueType()));
            paramMap.put(value.getCode(), constant.getValue());
        }
        return paramMap;
    }

    /**
     * 处理函数参数
     *
     * @param functionParamList 函数参数列表
     * @return list
     */
    private List<FunctionParam> getFunctionParam(List<PolicyFunctionParam> functionParamList) {
        List<FunctionParam> params = new ArrayList<>();
        if (CollUtil.isNotEmpty(functionParamList)) {
            params = functionParamList.stream().map(m -> {
                FunctionParam param = new FunctionParam();
                param.setName(m.getParamName());
                param.setCode(m.getParamCode());
                param.setValueType(m.getValueType());
                return param;
            }).collect(Collectors.toList());
        }
        return params;
    }
}
