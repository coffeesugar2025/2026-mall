package com.policy.web.service.generalrule.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.policy.web.listener.body.GeneralRuleMessageBody;
import com.policy.web.listener.event.GeneralRuleEvent;
import com.policy.web.service.*;
import com.policy.web.store.entity.PolicyGeneralRulePublish;
import com.policy.web.store.entity.PolicyRule;
import com.policy.web.store.mapper.PolicyGeneralRuleMapper;
import com.policy.web.store.mapper.PolicyRuleMapper;
import com.policy.web.vo.generalrule.*;
import com.policy.common.VersionUtils;
import com.policy.core.GeneralPolicy;
import com.policy.core.rule.GeneralRule;
import com.policy.web.config.Context;
import com.policy.common.enums.EnableEnum;
import com.policy.common.enums.DataStatus;
import com.policy.web.service.generalrule.GeneralRuleService;
import com.policy.web.store.entity.PolicyGeneralRule;
import com.policy.web.store.manager.*;


import com.policy.common.util.PageUtils;
import com.policy.web.vo.common.ViewRequest;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.common.vo.*;
import com.policy.core.condition.ConditionGroup;


import cn.hutool.core.lang.Validator;
import com.policy.common.exception.ValidException;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.workspace.Workspace;
import com.policy.web.vo.condition.ConfigValue;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/24
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class GeneralRuleServiceImpl implements GeneralRuleService {

    @Resource
    private PolicyRuleManager policyRuleManager;
    @Resource
    private PolicyGeneralRuleManager policyGeneralRuleManager;
    @Resource
    private PolicyGeneralRuleMapper policyGeneralRuleMapper;
    @Resource
    private PolicyRuleMapper policyRuleMapper;
    @Resource
    private GeneralPolicy generalpolicy;
    @Resource
    private ParameterService parameterService;
    @Resource
    private PolicyGeneralRulePublishManager policyGeneralRulePublishManager;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private ValueResolve valueResolve;
    @Resource
    private PolicyConditionGroupService policyConditionGroupService;
    @Resource
    private ConditionSetService conditionSetService;
    @Resource
    private ReferenceDataService referenceDataService;

    /**
     * 规则列表
     *
     * @param pageRequest 分页查询数据
     * @return page
     */
    @Override
    public PageResult<ListGeneralRuleResponse> list(PageRequest<ListGeneralRuleRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        return PageUtils.page(this.policyGeneralRuleManager, page, () -> {
            QueryWrapper<PolicyGeneralRule> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(PolicyGeneralRule::getWorkspaceId, workspace.getId());
            PageUtils.defaultOrder(orders, wrapper);
            ListGeneralRuleRequest query = pageRequest.getQuery();
            if (Validator.isNotEmpty(query.getName())) {
                wrapper.lambda().like(PolicyGeneralRule::getName, query.getName());
            }
            if (Validator.isNotEmpty(query.getCode())) {
                wrapper.lambda().like(PolicyGeneralRule::getCode, query.getCode());
            }
            // 遗留bug修复
            if (Validator.isNotEmpty(query.getStatus())) {
                if (query.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
                    wrapper.lambda().isNotNull(PolicyGeneralRule::getPublishVersion);
                } else {
                    wrapper.lambda().eq(PolicyGeneralRule::getStatus, query.getStatus());
                }
            }
            return wrapper;
        }, m -> {
            ListGeneralRuleResponse listRuleResponse = new ListGeneralRuleResponse();
            BeanUtil.copyProperties(m, listRuleResponse);
            return listRuleResponse;
        });
    }

    /**
     * 规则code是否存在
     *
     * @param code 规则code
     * @return true存在
     */
    @Override
    public Boolean ruleCodeIsExists(String code) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.policyGeneralRuleManager.lambdaQuery()
                .eq(PolicyGeneralRule::getWorkspaceId, workspace.getId())
                .eq(PolicyGeneralRule::getCode, code)
                .count();
        return count != null && count >= 1;
    }

    /**
     * 更新规则信息
     *
     * @param generalRuleBody 规则配置数据
     * @return true执行成功
     */
    @Override
    public Boolean updateRule(GeneralRuleBody generalRuleBody) {
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(generalRuleBody.getId());
        if (policyGeneralRule == null) {
            throw new ValidException("不存在规则:{}", generalRuleBody.getId());
        }
        // 如果之前是待发布，则删除原有待发布数据
        if (Objects.equals(policyGeneralRule.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
            this.policyGeneralRulePublishManager.lambdaUpdate()
                    .eq(PolicyGeneralRulePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                    .eq(PolicyGeneralRulePublish::getGeneralRuleId, generalRuleBody.getId())
                    .remove();
        }
        // 如果原来有条件信息，先删除原有信息
        this.policyConditionGroupService.removeConditionGroupByRuleIds(Collections.singletonList(policyGeneralRule.getRuleId()));
        // 保存条件信息
        this.policyConditionGroupService.saveConditionGroup(policyGeneralRule.getRuleId(), generalRuleBody.getConditionGroup());
        //  更新规则信息
        policyGeneralRule.setId(generalRuleBody.getId());
        policyGeneralRule.setStatus(DataStatus.EDIT.getStatus());
        // 保存规则结果
        this.saveAction(policyGeneralRule.getRuleId(), generalRuleBody.getAction());
        // 保存默认结果
        DefaultAction defaultAction = generalRuleBody.getDefaultAction();
        policyGeneralRule.setEnableDefaultAction(defaultAction.getEnableDefaultAction());
        policyGeneralRule.setDefaultActionValue(defaultAction.getValue());
        policyGeneralRule.setDefaultActionValueType(defaultAction.getValueType());
        policyGeneralRule.setDefaultActionType(defaultAction.getType());
        policyGeneralRule.setReferenceData(JSON.toJSONString(referenceDataService.countReferenceData(generalRuleBody)));
        this.policyGeneralRuleMapper.updateRuleById(policyGeneralRule);
        return true;
    }

    /**
     * 保存结果
     *
     * @param ruleId 规则id
     * @param action 结果
     */
    private void saveAction(Integer ruleId, ConfigValue action) {
        PolicyRule policyRule = new PolicyRule();
        policyRule.setId(ruleId);
        policyRule.setActionType(action.getType());
        policyRule.setActionValueType(action.getValueType());
        policyRule.setActionValue(action.getValue());
        this.policyRuleMapper.updateRuleById(policyRule);
    }

    /**
     * 删除规则
     *
     * @param id 规则id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(id);
        if (policyGeneralRule == null) {
            return false;
        }
        // 从引擎中移除规则
        if (this.generalpolicy.isExists(policyGeneralRule.getWorkspaceCode(), policyGeneralRule.getCode())) {
            GeneralRuleMessageBody ruleMessageBody = new GeneralRuleMessageBody();
            ruleMessageBody.setType(GeneralRuleMessageBody.Type.REMOVE);
            ruleMessageBody.setWorkspaceId(policyGeneralRule.getWorkspaceId());
            ruleMessageBody.setWorkspaceCode(policyGeneralRule.getWorkspaceCode());
            ruleMessageBody.setRuleCode(policyGeneralRule.getCode());
            this.eventPublisher.publishEvent(new GeneralRuleEvent(ruleMessageBody));
        }
        this.policyRuleManager.removeById(policyGeneralRule.getRuleId());
        // 删除规则发布记录
        this.policyGeneralRulePublishManager.lambdaUpdate().eq(PolicyGeneralRulePublish::getGeneralRuleId, id).remove();
        // 删除规则条件组信息
        this.policyConditionGroupService.removeConditionGroupByRuleIds(Collections.singletonList(policyGeneralRule.getRuleId()));
        // 删除规则
        return this.policyGeneralRuleManager.removeById(id);
    }

    /**
     * 保存或者更新规则定义信息
     *
     * @param ruleDefinition 规则定义信息
     * @return 规则id
     */
    @Override
    public Integer saveOrUpdateRuleDefinition(GeneralRuleDefinition ruleDefinition) {
        // 创建规则
        PolicyGeneralRule policyGeneralRule = new PolicyGeneralRule();
        if (ruleDefinition.getId() == null) {
            if (this.ruleCodeIsExists(ruleDefinition.getCode())) {
                throw new ValidException("规则Code：{}已经存在", ruleDefinition.getCode());
            }
            Workspace workspace = Context.getCurrentWorkspace();
            UserData userData = Context.getCurrentUser();
            policyGeneralRule.setCreateUserId(userData.getId());
            policyGeneralRule.setCreateUserName(userData.getUsername());
            policyGeneralRule.setWorkspaceId(workspace.getId());
            policyGeneralRule.setWorkspaceCode(workspace.getCode());
            PolicyRule policyRule = new PolicyRule();
            policyRule.setName(ruleDefinition.getName());
            policyRule.setCode(ruleDefinition.getCode());
            policyRule.setDescription(ruleDefinition.getDescription());
            policyRule.setCreateUserId(userData.getId());
            policyRule.setCreateUserName(userData.getUsername());
            this.policyRuleManager.save(policyRule);
            policyGeneralRule.setRuleId(policyRule.getId());
        } else {
            policyGeneralRule = this.policyGeneralRuleManager.lambdaQuery()
                    .eq(PolicyGeneralRule::getId, ruleDefinition.getId())
                    .one();
            if (policyGeneralRule == null) {
                throw new ValidException("不存在规则:{}", ruleDefinition.getId());
            } else {
                if (Objects.equals(policyGeneralRule.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
                    // 删除原有待发布规则
                    this.policyGeneralRulePublishManager.lambdaUpdate()
                            .eq(PolicyGeneralRulePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                            .eq(PolicyGeneralRulePublish::getGeneralRuleId, policyGeneralRule.getId())
                            .remove();
                }
            }
        }
        policyGeneralRule.setId(ruleDefinition.getId());
        policyGeneralRule.setName(ruleDefinition.getName());
        policyGeneralRule.setCode(ruleDefinition.getCode());
        policyGeneralRule.setDescription(ruleDefinition.getDescription());
        policyGeneralRule.setStatus(DataStatus.EDIT.getStatus());
        this.policyGeneralRuleManager.saveOrUpdate(policyGeneralRule);
        return policyGeneralRule.getId();
    }

    /**
     * 获取规则定义信息
     *
     * @param id 规则id
     * @return 规则定义信息
     */
    @Override
    public GeneralRuleDefinition getRuleDefinition(Integer id) {
        PolicyGeneralRule engineGeneralRule = this.policyGeneralRuleManager.lambdaQuery()
                .eq(PolicyGeneralRule::getId, id)
                .one();
        if (engineGeneralRule == null) {
            return null;
        }
        return BasicConversion.INSTANCE.convert(engineGeneralRule);
    }

    /**
     * 生成待发布版本，更新规则数据
     *
     * @param generalRuleBody 规则配置数据
     * @return true
     */
    @Override
    public Boolean generationRelease(GeneralRuleBody generalRuleBody) {
        // 更新规则
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(generalRuleBody.getId());
        if (policyGeneralRule == null) {
            throw new ValidException("不存在规则:{}", generalRuleBody.getId());
        }
        // 如果开启了默认结果
        DefaultAction defaultAction = generalRuleBody.getDefaultAction();
        defaultAction.valid();
        // 如果原来有条件信息，先删除原有信息
        this.policyConditionGroupService.removeConditionGroupByRuleIds(Collections.singletonList(policyGeneralRule.getRuleId()));
        // 保存条件信息
        this.policyConditionGroupService.saveConditionGroup(policyGeneralRule.getRuleId(), generalRuleBody.getConditionGroup());
        // 更新版本
        if (StrUtil.isBlank(policyGeneralRule.getCurrentVersion())) {
            policyGeneralRule.setCurrentVersion(VersionUtils.INIT_VERSION);
        } else {
            // 如果待发布与已经发布版本一致，则需要更新一个版本号
            if (policyGeneralRule.getCurrentVersion().equals(policyGeneralRule.getPublishVersion())) {
                // 获取下一个版本
                policyGeneralRule.setCurrentVersion(VersionUtils.getNextVersion(policyGeneralRule.getCurrentVersion()));
            }
        }
        //  更新规则信息
        policyGeneralRule.setStatus(DataStatus.WAIT_PUBLISH.getStatus());
        // 保存结果
        ConfigValue action = generalRuleBody.getAction();
        this.saveAction(policyGeneralRule.getRuleId(), generalRuleBody.getAction());
        // 保存默认结果
        policyGeneralRule.setEnableDefaultAction(defaultAction.getEnableDefaultAction());
        policyGeneralRule.setDefaultActionValue(defaultAction.getValue());
        policyGeneralRule.setDefaultActionValueType(defaultAction.getValueType());
        policyGeneralRule.setDefaultActionType(defaultAction.getType());
        String referenceData = JSON.toJSONString(referenceDataService.countReferenceData(generalRuleBody));
        policyGeneralRule.setReferenceData(referenceData);
        this.policyGeneralRuleMapper.updateRuleById(policyGeneralRule);
        // 添加新的待发布数据
        GeneralRule generalRule = new GeneralRule();
        generalRule.setId(generalRuleBody.getId());
        generalRule.setCode(policyGeneralRule.getCode());
        generalRule.setName(policyGeneralRule.getName());
        generalRule.setWorkspaceCode(policyGeneralRule.getWorkspaceCode());
        generalRule.setWorkspaceId(policyGeneralRule.getWorkspaceId());
        generalRule.setDescription(policyGeneralRule.getDescription());
        generalRule.setConditionSet(this.conditionSetService.loadConditionSet(generalRuleBody.getConditionGroup()));
        generalRule.setActionValue(this.valueResolve.getValue(action.getType(), action.getValueType(), action.getValue()));
        // 如果启用了默认结果
        if (EnableEnum.ENABLE.getStatus().equals(defaultAction.getEnableDefaultAction())) {
            generalRule.setDefaultActionValue(this.valueResolve.getValue(defaultAction.getType(), defaultAction.getValueType(), defaultAction.getValue()));
        }
        // 将不再判断是否存在待发布，直接执行删除sql
        this.policyGeneralRulePublishManager.lambdaUpdate()
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleId, policyGeneralRule.getId())
                .remove();
        // 生成待发布规则
        PolicyGeneralRulePublish rulePublish = new PolicyGeneralRulePublish();
        rulePublish.setGeneralRuleId(generalRule.getId());
        rulePublish.setGeneralRuleCode(generalRule.getCode());
        rulePublish.setData(generalRule.toJson());
        rulePublish.setStatus(DataStatus.WAIT_PUBLISH.getStatus());
        rulePublish.setWorkspaceId(generalRule.getWorkspaceId());
        // add version
        rulePublish.setVersion(policyGeneralRule.getCurrentVersion());
        rulePublish.setWorkspaceCode(generalRule.getWorkspaceCode());
        rulePublish.setReferenceData(referenceData);
        this.policyGeneralRulePublishManager.save(rulePublish);
        return true;
    }


    /**
     * 规则发布
     *
     * @param id 规则id
     * @return true
     */
    @Override
    public Boolean publish(Integer id) {
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(id);
        if (policyGeneralRule == null) {
            throw new ValidException("不存在规则:{}", id);
        }
        if (policyGeneralRule.getStatus().equals(DataStatus.EDIT.getStatus())) {
            throw new ValidException("该规则不可执行:{}", id);
        }
        // 如果已经是发布规则了
        if (policyGeneralRule.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            return true;
        }
        // 修改为已发布
        this.policyGeneralRuleManager.lambdaUpdate()
                .set(PolicyGeneralRule::getStatus, DataStatus.PUBLISHED.getStatus())
                // 更新发布的版本号
                .set(PolicyGeneralRule::getPublishVersion, policyGeneralRule.getCurrentVersion())
                .eq(PolicyGeneralRule::getId, policyGeneralRule.getId())
                .update();
        /*
         * 删除原有的已发布规则数据
         * 修改为，原有已发布为历史版本，后期准备回退
         */
        this.policyGeneralRulePublishManager.lambdaUpdate()
                .set(PolicyGeneralRulePublish::getStatus, DataStatus.HISTORY_PUBLISHED.getStatus())
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleId, policyGeneralRule.getId())
                .update();
        // 更新待发布为已发布
        this.policyGeneralRulePublishManager.lambdaUpdate()
                .set(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleId, policyGeneralRule.getId())
                .update();
        // 加载规则
        GeneralRuleMessageBody ruleMessageBody = new GeneralRuleMessageBody();
        ruleMessageBody.setType(GeneralRuleMessageBody.Type.LOAD);
        ruleMessageBody.setRuleCode(policyGeneralRule.getCode());
        ruleMessageBody.setWorkspaceId(policyGeneralRule.getWorkspaceId());
        ruleMessageBody.setWorkspaceCode(policyGeneralRule.getWorkspaceCode());
        this.eventPublisher.publishEvent(new GeneralRuleEvent(ruleMessageBody));
        return true;
    }

    /**
     * 获取规则信息
     *
     * @param id 规则id
     * @return 规则信息
     */
    @Override
    public GetGeneralRuleResponse getRuleConfig(Integer id) {
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(id);
        if (policyGeneralRule == null) {
            return null;
        }
        GetGeneralRuleResponse ruleResponse = new GetGeneralRuleResponse();
        ruleResponse.setId(policyGeneralRule.getId());
        ruleResponse.setCode(policyGeneralRule.getCode());
        ruleResponse.setName(policyGeneralRule.getName());
        ruleResponse.setDescription(policyGeneralRule.getDescription());
        ruleResponse.setConditionGroup(this.policyConditionGroupService.getConditionGroupConfig(policyGeneralRule.getRuleId()));
        // 结果
        PolicyRule policyRule = this.policyRuleManager.getById(policyGeneralRule.getRuleId());
        ConfigValue action = this.valueResolve.getConfigValue(policyRule.getActionValue(), policyRule.getActionType(), policyRule.getActionValueType());
        ruleResponse.setAction(action);
        // 默认结果
        ConfigValue defaultValue = this.valueResolve.getConfigValue(policyGeneralRule.getDefaultActionValue(), policyGeneralRule.getDefaultActionType(), policyGeneralRule.getDefaultActionValueType());
        DefaultAction defaultAction = new DefaultAction(defaultValue);
        defaultAction.setEnableDefaultAction(policyGeneralRule.getEnableDefaultAction());
        ruleResponse.setDefaultAction(defaultAction);
        return ruleResponse;
    }

    /**
     * 规则预览
     *
     * @param viewRequest 规则id
     * @return GetRuleResponse
     */
    @Override
    public ViewGeneralRuleResponse view(ViewRequest viewRequest) {
        Integer id = viewRequest.getId();
        PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(id);
        if (policyGeneralRule == null) {
            throw new ValidException("找不到预览的规则数据:{}", id);
        }
        if (policyGeneralRule.getStatus().equals(DataStatus.PUBLISHED.getStatus()) || viewRequest.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            PolicyGeneralRulePublish engineRulePublish = this.policyGeneralRulePublishManager.lambdaQuery()
                    .eq(PolicyGeneralRulePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyGeneralRulePublish::getGeneralRuleId, id)
                    .one();
            if (engineRulePublish == null) {
                throw new ValidException("找不到发布的规则:{}", id);
            }
            String data = engineRulePublish.getData();
            GeneralRule rule = GeneralRule.buildRule(data);
            return this.getRuleResponseProcess(rule);
        }
        PolicyGeneralRulePublish engineRulePublish = this.policyGeneralRulePublishManager.lambdaQuery()
                .eq(PolicyGeneralRulePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyGeneralRulePublish::getGeneralRuleId, id)
                .one();
        if (engineRulePublish == null) {
            throw new ValidException("找不到预览的规则数据:{}", id);
        }
        String data = engineRulePublish.getData();
        GeneralRule rule = GeneralRule.buildRule(data);
        return this.getRuleResponseProcess(rule);
    }

    /**
     * 解析Rule配置信息为ViewGeneralRuleResponse
     *
     * @param rule Rule
     * @return ViewGeneralRuleResponse
     */
    private ViewGeneralRuleResponse getRuleResponseProcess(GeneralRule rule) {
        ViewGeneralRuleResponse ruleResponse = new ViewGeneralRuleResponse();
        ruleResponse.setId(rule.getId());
        ruleResponse.setName(rule.getName());
        ruleResponse.setCode(rule.getCode());
        ruleResponse.setWorkspaceId(rule.getWorkspaceId());
        ruleResponse.setWorkspaceCode(rule.getWorkspaceCode());
        ruleResponse.setDescription(rule.getDescription());
        List<ConditionGroup> conditionGroups = rule.getConditionSet().getConditionGroups();
        ruleResponse.setConditionGroup(this.policyConditionGroupService.pressConditionGroupConfig(conditionGroups));
        ruleResponse.setAction(valueResolve.getConfigValue(rule.getActionValue()));
        DefaultAction defaultAction;
        if (rule.getDefaultActionValue() != null) {
            defaultAction = new DefaultAction(this.valueResolve.getConfigValue(rule.getDefaultActionValue()));
            defaultAction.setEnableDefaultAction(EnableEnum.ENABLE.getStatus());
        } else {
            defaultAction = new DefaultAction();
            defaultAction.setEnableDefaultAction(EnableEnum.DISABLE.getStatus());
        }
        ruleResponse.setDefaultAction(defaultAction);
        // 规则调用接口，以及规则入参
        ruleResponse.setParameters(this.parameterService.getParameters(rule));
        return ruleResponse;
    }


}
