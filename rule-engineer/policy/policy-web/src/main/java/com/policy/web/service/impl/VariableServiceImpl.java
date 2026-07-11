package com.policy.web.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.policy.web.listener.body.VariableMessageBody;
import com.policy.web.listener.event.VariableEvent;
import com.policy.web.config.Context;
import com.policy.web.service.VariableService;
import com.policy.web.store.entity.PolicyCondition;
import com.policy.web.store.entity.PolicyFunction;
import com.policy.web.store.entity.PolicyFunctionValue;
import com.policy.web.store.entity.PolicyVariable;
import com.policy.web.store.manager.*;
import com.policy.web.vo.variable.*;
import com.policy.common.util.PageUtils;
import com.policy.web.store.mapper.PolicyVariableMapper;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.common.vo.*;
import com.policy.web.vo.user.UserData;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.policy.common.exception.ValidException;
import com.policy.core.value.VariableType;
import com.policy.web.vo.workspace.Workspace;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/7/14
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class VariableServiceImpl implements VariableService {

    @Resource
    private PolicyVariableManager policyVariableManager;
    @Resource
    private PolicyFunctionValueManager policyFunctionValueManager;
    @Resource
    private PolicyFunctionManager policyFunctionManager;
    @Resource
    private PolicyVariableMapper policyVariableMapper;
    @Resource
    private PolicyElementManager policyElementManager;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private PolicyConditionManager policyConditionManager;
    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 添加变量
     *
     * @param addConditionRequest 变量信息
     * @return true
     */
    @Override
    public Boolean add(AddVariableRequest addConditionRequest) {
        if (this.varNameIsExists(addConditionRequest.getName())) {
            throw new ValidException("变量名称：{}已经存在", addConditionRequest.getName());
        }
        PolicyVariable engineVariable = new PolicyVariable();
        UserData userData = Context.getCurrentUser();
        engineVariable.setCreateUserId(userData.getId());
        engineVariable.setCreateUserName(userData.getUsername());
        engineVariable.setName(addConditionRequest.getName());
        engineVariable.setDescription(addConditionRequest.getDescription());
        engineVariable.setValueType(addConditionRequest.getValueType());
        engineVariable.setValue(addConditionRequest.getValue());
        engineVariable.setType(addConditionRequest.getType());
        Workspace workspace = Context.getCurrentWorkspace();
        engineVariable.setWorkspaceId(workspace.getId());
        this.policyVariableManager.save(engineVariable);
        if (addConditionRequest.getType().equals(VariableType.FUNCTION.getType())) {
            // 保存函数参数值
            List<ParamValue> paramValues = addConditionRequest.getParamValues();
            PolicyFunction policyFunction = this.getCurrentFunction(engineVariable.getValue());
            this.saveFunctionParamValues(policyFunction.getId(), engineVariable, paramValues);
        }
        // 通知加载变量
        VariableMessageBody variableMessageBody = new VariableMessageBody();
        variableMessageBody.setType(VariableMessageBody.Type.LOAD);
        variableMessageBody.setId(engineVariable.getId());
        this.eventPublisher.publishEvent(new VariableEvent(variableMessageBody));
        return true;
    }

    /**
     * 变量名称是否存在
     *
     * @param name 变量名称
     * @return true存在
     */
    @Override
    public Boolean varNameIsExists(String name) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.policyVariableManager.lambdaQuery()
                .eq(PolicyVariable::getWorkspaceId, workspace.getId())
                .eq(PolicyVariable::getName, name).count();
        return count != null && count >= 1;
    }

    /**
     * 保存函数参数值
     *
     * @param functionId     函数id
     * @param engineVariable 变量
     * @param paramValues    函数参数值
     */
    public void saveFunctionParamValues(Integer functionId, PolicyVariable engineVariable, List<ParamValue> paramValues) {
        List<PolicyFunctionValue> engineFunctionValues = paramValues.stream().map(m -> {
            // 变量自己不能引用自己
            if (Objects.equals(engineVariable.getName(), m.getName())) {
                throw new ValidException("变量不可以引用自身");
            }
            PolicyFunctionValue engineFunctionValue = new PolicyFunctionValue();
            engineFunctionValue.setFunctionId(functionId);
            engineFunctionValue.setParamCode(m.getCode());
            engineFunctionValue.setParamName(m.getName());
            engineFunctionValue.setVariableId(engineVariable.getId());
            engineFunctionValue.setType(m.getType());
            engineFunctionValue.setValueType(m.getValueType());
            engineFunctionValue.setValue(m.getValue());
            return engineFunctionValue;
        }).collect(Collectors.toList());
        this.policyFunctionValueManager.saveBatch(engineFunctionValues);
    }

    /**
     * 变量列表
     *
     * @param pageRequest param
     * @return result
     */
    @Override
    public PageResult<ListVariableResponse> list(PageRequest<ListVariableRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        return PageUtils.page(policyVariableManager, page, () -> {
            QueryWrapper<PolicyVariable> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(PolicyVariable::getWorkspaceId, workspace.getId());
            PageUtils.defaultOrder(orders, wrapper);

            ListVariableRequest query = pageRequest.getQuery();
            if (CollUtil.isNotEmpty(query.getValueType())) {
                wrapper.lambda().in(PolicyVariable::getValueType, query.getValueType());
            }
            if (Validator.isNotEmpty(query.getName())) {
                wrapper.lambda().like(PolicyVariable::getName, query.getName());
            }
            return wrapper;
        }, m -> {
            ListVariableResponse listVariableResponse = new ListVariableResponse();
            listVariableResponse.setId(m.getId());
            listVariableResponse.setName(m.getName());

            listVariableResponse.setValue(m.getValue());
            listVariableResponse.setType(m.getType());
            listVariableResponse.setValueType(m.getValueType());
            listVariableResponse.setDescription(m.getDescription());
            listVariableResponse.setCreateTime(m.getCreateTime());
            return listVariableResponse;
        });
    }

    /**
     * 变量分为固定值变量,函数变量
     *
     * @param id 变量id
     * @return var
     */
    @Override
    public GetVariableResponse get(Integer id) {
        PolicyVariable policyVariable = this.policyVariableManager.lambdaQuery()
                .eq(PolicyVariable::getId, id)
                .one();
        if (policyVariable == null) {
            return null;
        }
        GetVariableResponse variableResponse = BasicConversion.INSTANCE.convert(policyVariable);
        if (policyVariable.getType().equals(VariableType.CONSTANT.getType())) {
            return variableResponse;
        } else if (policyVariable.getType().equals(VariableType.FUNCTION.getType())) {
            String functionId = policyVariable.getValue();
            PolicyFunction engineFunction = policyFunctionManager.getById(functionId);
            GetVariableResponse.Function function = new GetVariableResponse.Function();
            function.setId(engineFunction.getId());
            function.setName(engineFunction.getName());
            function.setReturnValueType(engineFunction.getReturnValueType());
            // 处理函数入参值
            List<PolicyFunctionValue> functionValues = policyFunctionValueManager.lambdaQuery()
                    .eq(PolicyFunctionValue::getVariableId, id)
                    .eq(PolicyFunctionValue::getFunctionId, functionId).list();
            List<ParamValue> paramValueList = functionValues.stream().map(m -> {
                ParamValue paramValue = new ParamValue();
                paramValue.setName(m.getParamName());
                paramValue.setCode(m.getParamCode());
                paramValue.setType(m.getType());
                paramValue.setValue(m.getValue());
                paramValue.setValueType(m.getValueType());
                String valueName = m.getValue();
                if (VariableType.ELEMENT.getType().equals(m.getType())) {
                    valueName = policyElementManager.getById(m.getValue()).getName();
                } else if (VariableType.VARIABLE.getType().equals(m.getType())) {
                    PolicyVariable engineVariable = policyVariableManager.getById(m.getValue());
                    valueName = engineVariable.getName();
                }
                paramValue.setValueName(valueName);
                return paramValue;
            }).collect(Collectors.toList());
            function.setParamValues(paramValueList);
            variableResponse.setFunction(function);
        }
        return variableResponse;
    }

    /**
     * 根据id更新变量
     *
     * @param updateVariableRequest param
     * @return true
     */
    @Override
    public Boolean update(UpdateVariableRequest updateVariableRequest) {
        PolicyVariable policyVariable = this.policyVariableManager.lambdaQuery()
                .eq(PolicyVariable::getId, updateVariableRequest.getId())
                .one();
        if (policyVariable == null) {
            throw new ValidException("找不到更新的变量：{}", updateVariableRequest.getId());
        }
        if (!updateVariableRequest.getName().equals(policyVariable.getName())) {
            if (this.varNameIsExists(updateVariableRequest.getName())) {
                throw new ValidException("变量名称：{}已经存在", updateVariableRequest.getName());
            }
        }
        PolicyVariable engineVariable = new PolicyVariable();
        engineVariable.setId(updateVariableRequest.getId());
        engineVariable.setName(updateVariableRequest.getName());
        engineVariable.setDescription(updateVariableRequest.getDescription());
        engineVariable.setValue(updateVariableRequest.getValue());
        engineVariable.setType(updateVariableRequest.getType());
        this.policyVariableManager.updateById(engineVariable);
        // 函数信息
        if (updateVariableRequest.getType().equals(VariableType.FUNCTION.getType())) {
            // 删除原有的函数值信息
            this.policyFunctionValueManager.lambdaUpdate()
                    .eq(PolicyFunctionValue::getFunctionId, updateVariableRequest.getValue())
                    .eq(PolicyFunctionValue::getVariableId, engineVariable.getId())
                    .remove();
            PolicyFunction policyFunction = this.getCurrentFunction(engineVariable.getValue());
            // 保存函数参数值
            List<ParamValue> paramValues = updateVariableRequest.getParamValues();
            this.saveFunctionParamValues(policyFunction.getId(), engineVariable, paramValues);
        }
        // 通知加载变量
        VariableMessageBody variableMessageBody = new VariableMessageBody();
        variableMessageBody.setType(VariableMessageBody.Type.UPDATE);
        variableMessageBody.setId(engineVariable.getId());
        this.eventPublisher.publishEvent(new VariableEvent(variableMessageBody));
        return true;
    }

    public PolicyFunction getCurrentFunction(String functionId) {
        PolicyFunction engineFunction = this.policyFunctionManager.getById(functionId);
        if (engineFunction == null) {
            throw new ValidException("函数不存在");
        }
        String executor = engineFunction.getExecutor();
        if (!applicationContext.containsBean(executor)) {
            throw new ValidException("系统中找不到:{}函数Bean", executor);
        }
        return engineFunction;
    }

    /**
     * 根据id删除变量
     *
     * @param id 变量id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        PolicyVariable engineVariable = this.policyVariableManager.getById(id);
        if (engineVariable == null) {
            throw new ValidException("找不到要删除的变量：{}", id);
        }
        {
            Integer count = policyVariableMapper.countRuleVariable(id);
            if (count != null && count > 0) {
                throw new ValidException("有规则在引用此变量，无法删除");
            }
        }
        {
            Integer count = policyVariableMapper.countRuleSetVariable(id);
            if (count != null && count > 0) {
                throw new ValidException("有规则集在引用此变量，无法删除");
            }
        }
        {
            Integer count = policyVariableMapper.countPublishRuleVariable(id);
            if (count != null && count > 0) {
                throw new ValidException("有发布/待发布规则在引用此变量，无法删除");
            }
        }
        {
            Integer count = policyVariableMapper.countPublishRuleSetVariable(id);
            if (count != null && count > 0) {
                throw new ValidException("有发布/待发布规则集在引用此变量，无法删除");
            }
        }
        //  决策表引用
        {
            int reference = this.policyVariableMapper.countDecisionTableVariableId(id);
            if (reference > 0) {
                throw new ValidException("有决策表在引用此变量，无法删除");
            }
        }
        // 发布/待发布决策表引用
        {
            int reference = this.policyVariableMapper.countPublishDecisionTableVariableId(id);
            if (reference > 0) {
                throw new ValidException("有发布/待发布决策表在引用此变量，无法删除");
            }
        }
        {
            Integer count = this.policyFunctionValueManager.lambdaQuery()
                    .eq(PolicyFunctionValue::getType, VariableType.VARIABLE.getType())
                    .eq(PolicyFunctionValue::getValue, id).count();
            if (count != null && count > 0) {
                throw new ValidException("有函数值在引用此变量，无法删除");
            }
        }
        {
            Integer count = this.policyConditionManager.lambdaQuery()
                    .and(a -> a.or(o -> o.eq(PolicyCondition::getLeftType, VariableType.VARIABLE.getType())
                            .eq(PolicyCondition::getLeftValue, id))
                            .or(o -> o.eq(PolicyCondition::getRightType, VariableType.VARIABLE.getType())
                                    .eq(PolicyCondition::getRightValue, id)
                            )).count();
            if (count != null && count > 0) {
                throw new ValidException("有条件在引用此变量，无法删除");
            }
        }
        VariableMessageBody variableMessageBody = new VariableMessageBody();
        variableMessageBody.setType(VariableMessageBody.Type.REMOVE);
        variableMessageBody.setId(id);
        this.eventPublisher.publishEvent(new VariableEvent(variableMessageBody));
        // 删除变量函数值
        this.policyFunctionValueManager.lambdaUpdate()
                .eq(PolicyFunctionValue::getVariableId, engineVariable.getId())
                .remove();
        return policyVariableManager.removeById(id);
    }

}
