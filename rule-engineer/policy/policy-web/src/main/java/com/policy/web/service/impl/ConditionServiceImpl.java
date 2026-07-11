package com.policy.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.policy.web.store.entity.PolicyCondition;
import com.policy.web.store.entity.PolicyConditionGroupCondition;
import com.policy.web.store.entity.PolicyElement;
import com.policy.web.store.entity.PolicyVariable;
import com.policy.web.store.manager.PolicyElementManager;
import com.policy.web.store.manager.PolicyVariableManager;
import com.policy.web.vo.condition.*;
import com.policy.common.exception.ApiException;
import com.policy.common.exception.ValidException;
import com.policy.common.util.PageUtils;
import com.policy.common.vo.PageBase;
import com.policy.common.vo.PageRequest;
import com.policy.common.vo.PageResult;
import com.policy.common.vo.Rows;
import com.policy.core.DefaultInput;
import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.condition.Condition;
import com.policy.core.condition.Operator;
import com.policy.core.value.VariableType;
import com.policy.web.config.Context;
import com.policy.web.service.ConditionService;
import com.policy.web.service.ParameterService;
import com.policy.web.service.ValueResolve;
import com.policy.web.store.mapper.PolicyConditionMapper;
import com.policy.web.vo.common.ExecuteTestRequest;
import com.policy.web.vo.common.Parameter;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.variable.ParamValue;
import com.policy.web.vo.workspace.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2020/7/14
 * @since 1.0.0
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Component
public class ConditionServiceImpl implements ConditionService {

    @Resource
    private com.policy.web.store.manager.PolicyConditionManager PolicyConditionManager;
    @Resource
    private PolicyConditionMapper PolicyConditionMapper;
    @Resource
    private PolicyVariableManager policyVariableManager;
    @Resource
    private PolicyElementManager policyElementManager;
    @Resource
    private com.policy.web.store.manager.PolicyConditionGroupConditionManager PolicyConditionGroupConditionManager;
    @Resource
    private ValueResolve valueResolve;
    @Resource
    private PolicyConfiguration PolicyConfiguration;
    @Resource
    private ParameterService parameterService;


    /**
     * 保存条件
     *
     * @param addConditionRequest 条件配置信息
     * @return true
     */
    @Override
    public Boolean save(AddConditionRequest addConditionRequest) {
        if (this.conditionNameIsExists(addConditionRequest.getName())) {
            throw new ValidException("条件名称：{}已经存在", addConditionRequest.getName());
        }
        Workspace workspace = Context.getCurrentWorkspace();
        PolicyCondition condition = new PolicyCondition();
        UserData userData = Context.getCurrentUser();
        condition.setCreateUserId(userData.getId());
        condition.setCreateUserName(userData.getUsername());
        condition.setName(addConditionRequest.getName());
        condition.setDescription(addConditionRequest.getDescription());
        // 条件配置信息
        this.configBeanCopyToCondition(condition, addConditionRequest.getConfig());
        condition.setWorkspaceId(workspace.getId());
        return PolicyConditionManager.save(condition);
    }

    /**
     * 条件名称是否存在
     *
     * @param name 条件名称
     * @return true存在
     */
    @Override
    public Boolean conditionNameIsExists(String name) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.PolicyConditionManager.lambdaQuery()
                .eq(PolicyCondition::getWorkspaceId, workspace.getId())
                .eq(PolicyCondition::getName, name)
                .count();
        return count != null && count >= 1;
    }

    /**
     * 根绝id查询条件信息
     *
     * @param id 条件id
     * @return ConditionResponse
     */
    @Override
    public ConditionBody getById(Integer id) {
        PolicyCondition condition = this.PolicyConditionManager.lambdaQuery()
                .eq(PolicyCondition::getId, id)
                .one();
        if (condition == null) {
            throw new ApiException("根据Id:{},没有查询到数据", id);
        }
        return getConditionResponse(condition);
    }

    /**
     * 条件转换
     *
     * @param engineCondition engineCondition
     * @return ConditionResponse
     */
    @Override
    public ConditionBody getConditionResponse(PolicyCondition engineCondition) {
        ConditionBody conditionResponse = new ConditionBody();

        conditionResponse.setDescription(engineCondition.getDescription());
        conditionResponse.setId(engineCondition.getId());
        conditionResponse.setName(engineCondition.getName());

        ConfigBean configBean = getConfigBean(engineCondition);
        conditionResponse.setConfig(configBean);
        return conditionResponse;
    }

    /**
     * 条件转换
     *
     * @param engineCondition engineCondition
     * @param variableMap     条件用到的变量
     * @param elementMap      条件用到的元素
     * @return ConditionResponse
     */
    @Override
    public ConditionBody getConditionResponse(PolicyCondition engineCondition, Map<Integer, PolicyVariable> variableMap, Map<Integer, PolicyElement> elementMap) {
        ConditionBody conditionResponse = new ConditionBody();

        conditionResponse.setDescription(engineCondition.getDescription());
        conditionResponse.setId(engineCondition.getId());
        conditionResponse.setName(engineCondition.getName());

        ConfigBean configBean = getConfigBean(engineCondition, variableMap, elementMap);
        conditionResponse.setConfig(configBean);
        return conditionResponse;
    }

    /**
     * 条件配置信息
     *
     * @param engineCondition 条件
     * @return ConfigBean
     */
    public ConfigBean getConfigBean(PolicyCondition engineCondition) {
        ConfigBean configBean = new ConfigBean();

        ConfigValue leftValue = valueResolve.getConfigValue(engineCondition.getLeftValue(), engineCondition.getLeftType(), engineCondition.getLeftValueType());
        configBean.setLeftValue(leftValue);

        configBean.setSymbol(engineCondition.getSymbol());

        ConfigValue rightValue = valueResolve.getConfigValue(engineCondition.getRightValue(), engineCondition.getRightType(), engineCondition.getRightValueType());
        configBean.setRightValue(rightValue);
        return configBean;
    }


    /**
     * 条件列表
     *
     * @param pageRequest 分页查询信息
     * @return page
     */
    @Override
    public PageResult<ListConditionResponse> list(PageRequest<ListConditionRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        QueryWrapper<PolicyCondition> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(PolicyCondition::getWorkspaceId, workspace.getId());
        // 查询的数据排序
        PageUtils.defaultOrder(orders, wrapper);
        ListConditionRequest query = pageRequest.getQuery();
        if (Validator.isNotEmpty(query.getName())) {
            wrapper.lambda().like(PolicyCondition::getName, query.getName());
        }
        IPage<PolicyCondition> iPage = this.PolicyConditionManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), wrapper);
        List<PolicyCondition> engineConditions = iPage.getRecords();
        PageResult<ListConditionResponse> pageResult = new PageResult<>();
        if (CollUtil.isEmpty(engineConditions)) {
            return pageResult;
        }
        Map<Integer, PolicyElement> elementMap = this.getConditionElementMap(engineConditions);
        Map<Integer, PolicyVariable> variableMap = this.getConditionVariableMap(engineConditions);
        // 类型转换处理
        List<ListConditionResponse> conditionResponses = engineConditions.stream().map(m -> {
            ListConditionResponse listConditionResponse = new ListConditionResponse();
            listConditionResponse.setId(m.getId());
            listConditionResponse.setName(m.getName());
            listConditionResponse.setDescription(m.getDescription());
            listConditionResponse.setCreateTime(m.getCreateTime());
            ConfigBean configBean = this.getConfigBean(m, variableMap, elementMap);
            listConditionResponse.setConfig(configBean);
            // 获取符号说明
            String symbol = Operator.getByName(m.getSymbol()).getExplanation();
            listConditionResponse.setConfigInfo(String.format("%s %s %s", configBean.getLeftValue().getValueName(), symbol, configBean.getRightValue().getValueName()));
            return listConditionResponse;
        }).collect(Collectors.toList());
        pageResult.setData(new Rows<>(conditionResponses, PageUtils.getPageResponse(iPage)));
        return pageResult;
    }

    /**
     * 条件配置信息
     *
     * @param engineCondition 条件
     * @param variableMap     条件引用的变量
     * @param elementMap      条件引用的元素
     * @return ConfigBean
     */
    public ConfigBean getConfigBean(PolicyCondition engineCondition, Map<Integer, PolicyVariable> variableMap, Map<Integer, PolicyElement> elementMap) {
        ConfigBean configBean = new ConfigBean();

        ConfigValue leftValue = this.valueResolve.getConfigValue(engineCondition.getLeftValue(), engineCondition.getLeftType(), engineCondition.getLeftValueType(), variableMap, elementMap);
        configBean.setLeftValue(leftValue);

        configBean.setSymbol(engineCondition.getSymbol());

        ConfigValue rightValue = this.valueResolve.getConfigValue(engineCondition.getRightValue(), engineCondition.getRightType(), engineCondition.getRightValueType(), variableMap, elementMap);
        configBean.setRightValue(rightValue);
        return configBean;
    }

    /**
     * 获取条件中的变量
     *
     * @param PolicyConditions 条件信息
     * @return map
     */
    @Override
    public Map<Integer, PolicyVariable> getConditionVariableMap(Collection<PolicyCondition> PolicyConditions) {
        // 获取条件中的所有的变量数据
        Set<String> variableIds = new HashSet<>();
        for (PolicyCondition engineCondition : PolicyConditions) {
            Integer rightType = engineCondition.getRightType();
            if (rightType.equals(VariableType.VARIABLE.getType())) {
                variableIds.add(engineCondition.getRightValue());
            }
            Integer leftType = engineCondition.getLeftType();
            if (leftType.equals(VariableType.VARIABLE.getType())) {
                variableIds.add(engineCondition.getLeftValue());
            }
        }
        return Optional.of(variableIds).filter(CollUtil::isNotEmpty)
                .map(m -> policyVariableManager.lambdaQuery().in(PolicyVariable::getId, m).list()
                        .stream().collect(Collectors.toMap(PolicyVariable::getId, v -> v)))
                .orElse(new HashMap<>());
    }

    /**
     * 获取条件中的元素
     *
     * @param PolicyConditions 条件信息
     * @return map
     */
    @Override
    public Map<Integer, PolicyElement> getConditionElementMap(Collection<PolicyCondition> PolicyConditions) {
        Set<String> elementIds = new HashSet<>();
        for (PolicyCondition engineCondition : PolicyConditions) {
            Integer rightType = engineCondition.getRightType();
            if (rightType.equals(VariableType.ELEMENT.getType())) {
                elementIds.add(engineCondition.getRightValue());
            }
            Integer leftType = engineCondition.getLeftType();
            if (leftType.equals(VariableType.ELEMENT.getType())) {
                elementIds.add(engineCondition.getLeftValue());
            }
        }
        return Optional.of(elementIds).filter(CollUtil::isNotEmpty)
                .map(m -> policyElementManager.lambdaQuery().in(PolicyElement::getId, m).list()
                        .stream().collect(Collectors.toMap(PolicyElement::getId, v -> v)))
                .orElse(new HashMap<>());
    }


    /**
     * 更新条件
     *
     * @param updateConditionRequest 更新条件
     * @return true
     */
    @Override
    public Boolean update(UpdateConditionRequest updateConditionRequest) {
        PolicyCondition policyCondition = this.PolicyConditionManager.lambdaQuery()
                .eq(PolicyCondition::getId, updateConditionRequest.getId())
                .one();
        if (policyCondition == null) {
            throw new ValidException("规则条件找不到：{}", updateConditionRequest.getId());
        }
        if (!policyCondition.getName().equals(updateConditionRequest.getName())) {
            if (this.conditionNameIsExists(updateConditionRequest.getName())) {
                throw new ValidException("条件名称：{}已经存在", updateConditionRequest.getName());
            }
        }
        Integer conditionId = updateConditionRequest.getId();
        // 更新条件
        PolicyCondition condition = new PolicyCondition();
        condition.setId(conditionId);
        condition.setName(updateConditionRequest.getName());
        condition.setDescription(updateConditionRequest.getDescription());
        // 条件配置信息
        configBeanCopyToCondition(condition, updateConditionRequest.getConfig());
        this.PolicyConditionManager.updateById(condition);
        // 更新引用此规则的条件到编辑中
        this.PolicyConditionMapper.updateRuleWaitPublish(conditionId);
        // 规则集引用的
        this.PolicyConditionMapper.updateRuleSetWaitPublish(conditionId);
        return true;
    }

    /**
     * 从ConfigBean信息复制到PolicyCondition
     *
     * @param condition PolicyCondition
     * @param config    请求的条件配置数据
     */
    private void configBeanCopyToCondition(PolicyCondition condition, ConfigBean config) {
        ConfigValue leftValue = config.getLeftValue();
        condition.setLeftValueType(leftValue.getValueType());
        condition.setLeftType(leftValue.getType());
        condition.setLeftValue(leftValue.getValue());
        String symbol = config.getSymbol();
        condition.setSymbol(symbol);
        ConfigValue rightValue = config.getRightValue();
        condition.setRightValueType(rightValue.getValueType());
        condition.setRightType(rightValue.getType());
        condition.setRightValue(rightValue.getValue());
    }

    /**
     * 删除条件
     *
     * @param id 条件id
     * @return true：删除成功
     */
    @Override
    public Boolean delete(Integer id) {
        Integer count = PolicyConditionGroupConditionManager.lambdaQuery()
                .eq(PolicyConditionGroupCondition::getConditionId, id)
                .count();
        if (count != null && count > 0) {
            throw new ValidException("有规则/规则集在引用此条件，无法删除");
        }
        return this.PolicyConditionManager.removeById(id);
    }

    /**
     * 根据id获取条件中的元素
     *
     * @param id 条件id
     * @return list
     */
    @Override
    public Set<Parameter> getParameter(Integer id) {
        Workspace workspace = Context.getCurrentWorkspace();
        PolicyCondition policyCondition = this.PolicyConditionManager.lambdaQuery()
                .eq(PolicyCondition::getId, id)
                .eq(PolicyCondition::getWorkspaceId, workspace.getId())
                .one();
        if (policyCondition == null) {
            throw new ValidException("规则条件找不到：{}", id);
        }
        return this.parameterService.getParameter(policyCondition);
    }

    /**
     * 测试运行条件
     *
     * @param executeTestRequest 参数
     * @return true/false
     */
    @Override
    public Boolean run(ExecuteTestRequest executeTestRequest) {
        Integer conditionId = executeTestRequest.getId();
        PolicyCondition PolicyCondition = this.PolicyConditionManager.getById(conditionId);
        if (PolicyCondition == null) {
            throw new ValidException("规则条件找不到：{}", conditionId);
        }
        List<ParamValue> paramValues = executeTestRequest.getParamValues();
        Input input = new DefaultInput();
        if (CollUtil.isNotEmpty(paramValues)) {
            for (ParamValue paramValue : paramValues) {
                input.put(paramValue.getCode(), paramValue.getValue());
            }
        }
        PolicyConfiguration configuration = new PolicyConfiguration();
        configuration.setEngineVariable(this.PolicyConfiguration.getEngineVariable());
        Condition condition = new Condition();
        condition.setId(PolicyCondition.getId());
        condition.setName(PolicyCondition.getName());
        condition.setLeftValue(this.valueResolve.getValue(PolicyCondition.getLeftType(), PolicyCondition.getLeftValueType(), PolicyCondition.getLeftValue()));
        condition.setOperator(Operator.getByName(PolicyCondition.getSymbol()));
        condition.setRightValue(this.valueResolve.getValue(PolicyCondition.getRightType(), PolicyCondition.getRightValueType(), PolicyCondition.getRightValue()));
        Condition.verify(condition);
        return condition.compare(input, configuration);
    }


}
