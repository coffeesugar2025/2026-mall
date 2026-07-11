package com.policy.web.service.ruleset.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.policy.common.VersionUtils;
import com.policy.common.enums.DataStatus;
import com.policy.common.enums.EnableEnum;
import com.policy.common.exception.ValidException;
import com.policy.common.util.PageUtils;
import com.policy.common.vo.PageBase;
import com.policy.common.vo.PageRequest;
import com.policy.common.vo.PageResult;
import com.policy.core.RuleSetEngine;
import com.policy.core.condition.ConditionGroup;
import com.policy.core.condition.ConditionSet;
import com.policy.core.rule.Rule;
import com.policy.core.rule.RuleSet;
import com.policy.core.rule.RuleSetStrategyType;
import com.policy.web.config.Context;
import com.policy.web.listener.body.RuleSetMessageBody;
import com.policy.web.listener.event.RuleSetEvent;
import com.policy.web.service.ConditionSetService;
import com.policy.web.service.ParameterService;
import com.policy.web.service.ReferenceDataService;
import com.policy.web.service.ValueResolve;
import com.policy.web.service.ruleset.RuleSetService;
import com.policy.web.store.entity.*;
import com.policy.web.store.manager.*;
import com.policy.web.vo.common.ViewRequest;
import com.policy.web.vo.condition.ConditionGroupConfig;
import com.policy.web.vo.condition.ConfigValue;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.web.vo.generalrule.ListGeneralRuleRequest;
import com.policy.web.vo.ruleset.*;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.workspace.Workspace;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.policy.web.service.PolicyConditionGroupService;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2021/1/14
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class RuleSetServiceImpl implements RuleSetService {

    @Resource
    private RuleSetEngine ruleSetEngine;
    @Resource
    private PolicyRuleSetManager policyRuleSetManager;
    @Resource
    private PolicyRuleSetPublishManager policyRuleSetPublishManager;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private PolicyConditionGroupService policyConditionGroupService;
    @Resource
    private PolicyRuleManager policyRuleManager;
    @Resource
    private PolicyRuleSetRuleManager policyRuleSetRuleManager;
    @Resource
    private ParameterService parameterService;
    @Resource
    private ValueResolve valueResolve;
    @Resource
    private ConditionSetService conditionSetService;
    @Resource
    private ReferenceDataService referenceDataService;

    /**
     * 获取规则集列表
     *
     * @param pageRequest 分页查询参数
     * @return page
     */
    @Override
    public PageResult<ListRuleSetResponse> list(PageRequest<ListGeneralRuleRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        return PageUtils.page(this.policyRuleSetManager, page, () -> {
            QueryWrapper<PolicyRuleSet> wrapper = new QueryWrapper<>();
            PageUtils.defaultOrder(orders, wrapper);
            LambdaQueryWrapper<PolicyRuleSet> lambda = wrapper.lambda();
            lambda.eq(PolicyRuleSet::getWorkspaceId, workspace.getId());
            ListGeneralRuleRequest query = pageRequest.getQuery();
            if (Validator.isNotEmpty(query.getCode())) {
                lambda.like(PolicyRuleSet::getCode, query.getCode());
            }
            if (Validator.isNotEmpty(query.getName())) {
                lambda.like(PolicyRuleSet::getName, query.getName());
            }
            // 遗留bug修复
            if (Validator.isNotEmpty(query.getStatus())) {
                if (query.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
                    lambda.isNotNull(PolicyRuleSet::getPublishVersion);
                } else {
                    lambda.eq(PolicyRuleSet::getStatus, query.getStatus());
                }
            }
            return wrapper;
        }, m -> {
            ListRuleSetResponse listRuleSetResponse = new ListRuleSetResponse();
            BeanUtil.copyProperties(m, listRuleSetResponse);
            return listRuleSetResponse;
        });
    }

    /**
     * 保存或者更新规则集定义信息
     *
     * @param ruleSetDefinition 规则集定义信息
     * @return 规则集id
     */
    @Override
    public Integer saveOrUpdateRuleSetDefinition(RuleSetDefinition ruleSetDefinition) {
        // 创建规则
        PolicyRuleSet policyGeneralRule = new PolicyRuleSet();
        if (ruleSetDefinition.getId() == null) {
            if (this.ruleSetCodeIsExists(ruleSetDefinition.getCode())) {
                throw new ValidException("规则集Code：{}已经存在", ruleSetDefinition.getCode());
            }
            Workspace workspace = Context.getCurrentWorkspace();
            UserData userData = Context.getCurrentUser();
            policyGeneralRule.setCreateUserId(userData.getId());
            policyGeneralRule.setCreateUserName(userData.getUsername());
            policyGeneralRule.setWorkspaceId(workspace.getId());
            policyGeneralRule.setWorkspaceCode(workspace.getCode());
            // 初始化默认策略
            policyGeneralRule.setStrategyType(RuleSetStrategyType.ALL_RULE.getValue());
        } else {
            policyGeneralRule = this.policyRuleSetManager.lambdaQuery()
                    .eq(PolicyRuleSet::getId, ruleSetDefinition.getId())
                    .one();
            if (policyGeneralRule == null) {
                throw new ValidException("不存在规则集:{}", ruleSetDefinition.getId());
            } else {
                if (Objects.equals(policyGeneralRule.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
                    // 删除原有待发布规则
                    this.policyRuleSetPublishManager.lambdaUpdate()
                            .eq(PolicyRuleSetPublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                            .eq(PolicyRuleSetPublish::getRuleSetId, policyGeneralRule.getId())
                            .remove();
                }
            }
        }
        policyGeneralRule.setId(ruleSetDefinition.getId());
        policyGeneralRule.setName(ruleSetDefinition.getName());
        policyGeneralRule.setCode(ruleSetDefinition.getCode());
        policyGeneralRule.setDescription(ruleSetDefinition.getDescription());
        policyGeneralRule.setStatus(DataStatus.EDIT.getStatus());
        this.policyRuleSetManager.saveOrUpdate(policyGeneralRule);
        return policyGeneralRule.getId();
    }

    /**
     * 获取规则集定义信息
     *
     * @param id 规则集id
     * @return 规则集定义信息
     */
    @Override
    public RuleSetDefinition getRuleSetDefinition(Integer id) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.lambdaQuery()
                .eq(PolicyRuleSet::getId, id)
                .one();
        if (policyRuleSet == null) {
            return null;
        }
        return BasicConversion.INSTANCE.convert(policyRuleSet);
    }

    /**
     * 生成待发布版本，更新规则数据
     *
     * @param ruleSetBody 规则集配置数据
     * @return true
     */
    @Override
    public Boolean generationRelease(RuleSetBody ruleSetBody) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(ruleSetBody.getId());
        if (policyRuleSet == null) {
            throw new ValidException("不存在规则集:{}", ruleSetBody.getId());
        }
        RuleBody defaultRule = ruleSetBody.getDefaultRule();
        if (Objects.equals(ruleSetBody.getEnableDefaultRule(), EnableEnum.ENABLE.getStatus())) {
            ConfigValue action = defaultRule.getAction();
            if (Validator.isEmpty(action.getType())) {
                throw new ValidException("规则集默认规则结果类型不能为空");
            }
            if (Validator.isEmpty(action.getValueType())) {
                throw new ValidException("规则集默认规则结果值类型不能为空");
            }
            if (Validator.isEmpty(action.getValue())) {
                throw new ValidException("规则集默认规则结果值不能为空");
            }
        }
        // 更新版本
        if (StrUtil.isBlank(policyRuleSet.getCurrentVersion())) {
            policyRuleSet.setCurrentVersion(VersionUtils.INIT_VERSION);
        } else {
            // 如果待发布与已经发布版本一致，则需要更新一个版本号
            if (policyRuleSet.getCurrentVersion().equals(policyRuleSet.getPublishVersion())) {
                // 获取下一个版本
                policyRuleSet.setCurrentVersion(VersionUtils.getNextVersion(policyRuleSet.getCurrentVersion()));
            }
        }
        policyRuleSet.setStrategyType(ruleSetBody.getStrategyType());
        policyRuleSet.setStatus(DataStatus.WAIT_PUBLISH.getStatus());
        policyRuleSet.setEnableDefaultRule(ruleSetBody.getEnableDefaultRule());
        String referenceData = JSON.toJSONString(referenceDataService.countReferenceData(ruleSetBody));
        policyRuleSet.setReferenceData(referenceData);
        // 以下代码性能可优化
        this.deleteRuleSetRule(policyRuleSet);
        // 绑定新的
        this.bindNewRuleSet(ruleSetBody.getRuleSet(), policyRuleSet.getId());
        if (defaultRule != null) {
            Integer defaultRuleId = this.saveRule(defaultRule);
            policyRuleSet.setDefaultRuleId(defaultRuleId);
        }
        this.policyRuleSetManager.updateById(policyRuleSet);
        // 添加新的待发布数据
        RuleSet ruleSet = new RuleSet();
        ruleSet.setId(policyRuleSet.getId());
        ruleSet.setName(policyRuleSet.getName());
        ruleSet.setCode(policyRuleSet.getCode());
        ruleSet.setWorkspaceId(policyRuleSet.getWorkspaceId());
        ruleSet.setWorkspaceCode(policyRuleSet.getWorkspaceCode());
        ruleSet.setDescription(policyRuleSet.getDescription());
        ruleSet.setStrategyType(RuleSetStrategyType.getByValue(ruleSetBody.getStrategyType()));
        List<RuleBody> bodyList = ruleSetBody.getRuleSet();
        for (RuleBody ruleBody : bodyList) {
            Rule rule = this.getRule(ruleBody);
            ruleSet.addRule(rule);
        }
        // 如果启用了默认规则
        if (EnableEnum.ENABLE.getStatus().equals(ruleSetBody.getEnableDefaultRule())) {
            RuleBody defaultRuleBody = ruleSetBody.getDefaultRule();
            Rule rule = this.getRule(defaultRuleBody);
            ruleSet.setDefaultRule(rule);
        }
        // 如果之前是待发布，则删除原有待发布数据
        this.policyRuleSetPublishManager.lambdaUpdate()
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetId, ruleSetBody.getId())
                .remove();
        // 生成新的待发布
        PolicyRuleSetPublish ruleSetPublish = new PolicyRuleSetPublish();
        ruleSetPublish.setRuleSetId(ruleSet.getId());
        ruleSetPublish.setRuleSetCode(ruleSet.getCode());
        ruleSetPublish.setData(ruleSet.toJson());
        ruleSetPublish.setStatus(DataStatus.WAIT_PUBLISH.getStatus());
        ruleSetPublish.setWorkspaceId(ruleSet.getWorkspaceId());
        ruleSetPublish.setWorkspaceCode(ruleSet.getWorkspaceCode());
        ruleSetPublish.setReferenceData(referenceData);
        // add version
        ruleSetPublish.setVersion(policyRuleSet.getCurrentVersion());
        this.policyRuleSetPublishManager.save(ruleSetPublish);
        return true;
    }

    private Rule getRule(RuleBody ruleBody) {
        if (ruleBody == null) {
            return null;
        }
        Rule rule = new Rule();
        rule.setId(ruleBody.getId());
        rule.setName(ruleBody.getName());
        List<ConditionGroupConfig> conditionGroup = ruleBody.getConditionGroup();
        ConditionSet conditionSet = this.conditionSetService.loadConditionSet(conditionGroup);
        rule.setConditionSet(conditionSet);
        ConfigValue action = ruleBody.getAction();
        rule.setActionValue(this.valueResolve.getValue(action.getType(), action.getValueType(), action.getValue()));
        return rule;
    }

    /**
     * 绑定新的规则集规则
     *
     * @param ruleSet   规则集
     * @param ruleSetId 规则集合id
     */
    private void bindNewRuleSet(List<RuleBody> ruleSet, Integer ruleSetId) {
        List<PolicyRuleSetRule> policyRuleSetRules = new ArrayList<>();
        for (RuleBody ruleBody : ruleSet) {
            Integer ruleId = this.saveRule(ruleBody);
            Integer orderNo = ruleBody.getOrderNo();
            PolicyRuleSetRule policyRuleSetRule = new PolicyRuleSetRule();
            policyRuleSetRule.setRuleSetId(ruleSetId);
            policyRuleSetRule.setRuleId(ruleId);
            policyRuleSetRule.setOrderNo(orderNo);
            policyRuleSetRules.add(policyRuleSetRule);
        }
        this.policyRuleSetRuleManager.saveBatch(policyRuleSetRules);
    }

    /**
     * 规则集发布
     *
     * @param id 规则集id
     * @return true
     */
    @Override
    public Boolean publish(Integer id) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(id);
        if (policyRuleSet == null) {
            throw new ValidException("不存在规则集:{}", id);
        }
        if (policyRuleSet.getStatus().equals(DataStatus.EDIT.getStatus())) {
            throw new ValidException("该规则集不可执行:{}", id);
        }
        // 如果已经是发布规则了
        if (policyRuleSet.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            return true;
        }
        // 修改为已发布
        this.policyRuleSetManager.lambdaUpdate()
                .set(PolicyRuleSet::getStatus, DataStatus.PUBLISHED.getStatus())
                // 更新发布的版本号
                .set(PolicyRuleSet::getPublishVersion, policyRuleSet.getCurrentVersion())
                .eq(PolicyRuleSet::getId, policyRuleSet.getId())
                .update();
        /*
         * 删除原有的已发布规则集数据
         * 修改为，原有已发布为历史版本，后期准备回退
         */
        this.policyRuleSetPublishManager.lambdaUpdate()
                .set(PolicyRuleSetPublish::getStatus, DataStatus.HISTORY_PUBLISHED.getStatus())
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetId, policyRuleSet.getId())
                .update();
        // 更新待发布为已发布
        this.policyRuleSetPublishManager.lambdaUpdate()
                .set(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetId, policyRuleSet.getId())
                .update();
        // 加载规则集
        RuleSetMessageBody ruleSetMessageBody = new RuleSetMessageBody();
        ruleSetMessageBody.setType(RuleSetMessageBody.Type.LOAD);
        ruleSetMessageBody.setRuleSetCode(policyRuleSet.getCode());
        ruleSetMessageBody.setWorkspaceId(policyRuleSet.getWorkspaceId());
        ruleSetMessageBody.setWorkspaceCode(policyRuleSet.getWorkspaceCode());
        this.eventPublisher.publishEvent(new RuleSetEvent(ruleSetMessageBody));
        return true;
    }

    /**
     * 更新规则集信息
     *
     * @param ruleSetBody 规则配置数据
     * @return true执行成功
     */
    @Override
    public Boolean updateRuleSet(RuleSetBody ruleSetBody) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(ruleSetBody.getId());
        if (policyRuleSet == null) {
            throw new ValidException("不存在规则集:{}", ruleSetBody.getId());
        }
        // 如果之前是待发布，则删除原有待发布数据
        if (Objects.equals(policyRuleSet.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
            this.policyRuleSetPublishManager.lambdaUpdate()
                    .eq(PolicyRuleSetPublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                    .eq(PolicyRuleSetPublish::getRuleSetId, ruleSetBody.getId())
                    .remove();
        }
        policyRuleSet.setStrategyType(ruleSetBody.getStrategyType());
        policyRuleSet.setStatus(DataStatus.EDIT.getStatus());
        policyRuleSet.setEnableDefaultRule(ruleSetBody.getEnableDefaultRule());
        policyRuleSet.setReferenceData(JSON.toJSONString(referenceDataService.countReferenceData(ruleSetBody)));
        List<RuleBody> ruleSet = ruleSetBody.getRuleSet();
        // 以下代码性能可优化
        this.deleteRuleSetRule(policyRuleSet);
        // 绑定新的
        this.bindNewRuleSet(ruleSet, policyRuleSet.getId());
        RuleBody defaultRule = ruleSetBody.getDefaultRule();
        if (defaultRule != null) {
            Integer defaultRuleId = this.saveRule(defaultRule);
            policyRuleSet.setDefaultRuleId(defaultRuleId);
        }
        this.policyRuleSetManager.updateById(policyRuleSet);
        return true;
    }

    /**
     * 删除老的规则集规则关系
     *
     * @param policyRuleSet policyRuleSet
     */
    public void deleteRuleSetRule(PolicyRuleSet policyRuleSet) {
        // 删除老的规则集规则关系
        List<PolicyRuleSetRule> engineRuleSetRules = this.policyRuleSetRuleManager.lambdaQuery()
                .eq(PolicyRuleSetRule::getRuleSetId, policyRuleSet.getId())
                .list();
        if (CollUtil.isNotEmpty(engineRuleSetRules)) {
            this.policyRuleSetRuleManager.removeByIds(engineRuleSetRules.stream().map(PolicyRuleSetRule::getId).collect(Collectors.toList()));
            List<Integer> ruleIds = engineRuleSetRules.stream().map(PolicyRuleSetRule::getRuleId).collect(Collectors.toList());
            ruleIds.add(policyRuleSet.getDefaultRuleId());
            this.policyRuleManager.removeByIds(ruleIds);
            // 删除规则集条件组
            this.policyConditionGroupService.removeConditionGroupByRuleIds(ruleIds);
        }
    }


    /**
     * 保存规则并返回规则id
     *
     * @param ruleBody 规则体
     * @return 规则id
     */
    private Integer saveRule(RuleBody ruleBody) {
        PolicyRule policyRule = new PolicyRule();
        policyRule.setName(ruleBody.getName());
        ConfigValue action = ruleBody.getAction();
        if (action != null) {
            policyRule.setActionType(action.getType());
            policyRule.setActionValueType(action.getValueType());
            policyRule.setActionValue(action.getValue());
        }
        this.policyRuleManager.save(policyRule);
        List<ConditionGroupConfig> conditionGroup = ruleBody.getConditionGroup();
        if (CollUtil.isNotEmpty(conditionGroup)) {
            this.policyConditionGroupService.saveConditionGroup(policyRule.getId(), conditionGroup);
        }
        return policyRule.getId();
    }

    /**
     * 获取规则集信息
     *
     * @param id 规则集id
     * @return 规则集信息
     */
    @Override
    public GetRuleSetResponse getRuleSetConfig(Integer id) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(id);
        if (policyRuleSet == null) {
            throw new ValidException("不存在规则集:{}", id);
        }
        GetRuleSetResponse ruleSetResponse = new GetRuleSetResponse();
        ruleSetResponse.setId(policyRuleSet.getId());
        ruleSetResponse.setName(policyRuleSet.getName());
        ruleSetResponse.setCode(policyRuleSet.getCode());
        ruleSetResponse.setDescription(policyRuleSet.getDescription());
        ruleSetResponse.setWorkspaceId(policyRuleSet.getWorkspaceId());
        ruleSetResponse.setWorkspaceCode(policyRuleSet.getWorkspaceCode());
        ruleSetResponse.setStrategyType(policyRuleSet.getStrategyType());
        // 先做功能后期优化
        List<PolicyRuleSetRule> policyRuleSetRules = this.policyRuleSetRuleManager.lambdaQuery().eq(PolicyRuleSetRule::getRuleSetId, id).list();
        List<Integer> ruleIds = policyRuleSetRules.stream().map(PolicyRuleSetRule::getRuleId).collect(Collectors.toList());
        ruleIds.add(policyRuleSet.getDefaultRuleId());
        Map<Integer, PolicyRule> policyRuleMap = this.policyRuleManager.lambdaQuery().in(PolicyRule::getId, ruleIds).list()
                .stream().collect(Collectors.toMap(PolicyRule::getId, Function.identity()));
        List<RuleBody> ruleSet = new ArrayList<>();
        if (CollUtil.isNotEmpty(policyRuleSetRules)) {
            for (PolicyRuleSetRule policyRuleSetRule : policyRuleSetRules) {
                PolicyRule policyRule = policyRuleMap.get(policyRuleSetRule.getRuleId());
                RuleBody ruleBody = this.getRuleBody(policyRule, policyRuleSetRule.getOrderNo());
                ruleSet.add(ruleBody);
            }
        }
        ruleSetResponse.setRuleSet(ruleSet);
        if (policyRuleSet.getDefaultRuleId() != null) {
            PolicyRule policyRule = policyRuleMap.get(policyRuleSet.getDefaultRuleId());
            ruleSetResponse.setDefaultRule(this.getRuleBody(policyRule, null));
        } else {
            ruleSetResponse.setDefaultRule(new RuleBody());
        }
        ruleSetResponse.setEnableDefaultRule(policyRuleSet.getEnableDefaultRule());
        return ruleSetResponse;
    }

    /**
     * 获取规则body
     *
     * @param policyRule 规则
     * @param orderNo        规则集顺序
     * @return RuleBody
     */
    public RuleBody getRuleBody(PolicyRule policyRule, Integer orderNo) {
        RuleBody ruleBody = new RuleBody();
        ruleBody.setId(policyRule.getId());
        ruleBody.setName(policyRule.getName());
        ruleBody.setOrderNo(orderNo);
        ruleBody.setConditionGroup(this.policyConditionGroupService.getConditionGroupConfig(policyRule.getId()));
        ruleBody.setAction(this.valueResolve.getConfigValue(policyRule.getActionValue(), policyRule.getActionType(), policyRule.getActionValueType()));
        return ruleBody;
    }

    /**
     * 规则集预览
     *
     * @param viewRequest 规则集id
     * @return GetRuleResponse
     */
    @Override
    public ViewRuleSetResponse view(ViewRequest viewRequest) {
        Integer id = viewRequest.getId();
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(id);
        if (policyRuleSet == null) {
            throw new ValidException("找不到预览的规则集数据:{}", id);
        }
        // 如果只有已发布
        if (policyRuleSet.getStatus().equals(DataStatus.PUBLISHED.getStatus()) || viewRequest.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            PolicyRuleSetPublish ruleSetPublish = this.policyRuleSetPublishManager.lambdaQuery()
                    .eq(PolicyRuleSetPublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyRuleSetPublish::getRuleSetId, id)
                    .one();
            if (ruleSetPublish == null) {
                throw new ValidException("找不到发布的规则集:{}", id);
            }
            String data = ruleSetPublish.getData();
            RuleSet ruleSet = RuleSet.buildRuleSet(data);
            return this.getRuleSetResponseProcess(ruleSet);
        }
        PolicyRuleSetPublish ruleSetPublish = this.policyRuleSetPublishManager.lambdaQuery()
                .eq(PolicyRuleSetPublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyRuleSetPublish::getRuleSetId, id)
                .one();
        if (ruleSetPublish == null) {
            throw new ValidException("找不到预览的规则集数据:{}", id);
        }
        String data = ruleSetPublish.getData();
        RuleSet ruleSet = RuleSet.buildRuleSet(data);
        return this.getRuleSetResponseProcess(ruleSet);
    }

    /**
     * 解析Rule set配置信息为ViewRuleSetResponse
     *
     * @param ruleSet ruleSet
     * @return ViewRuleSetResponse
     */
    private ViewRuleSetResponse getRuleSetResponseProcess(RuleSet ruleSet) {
        ViewRuleSetResponse viewRuleSetResponse = new ViewRuleSetResponse();
        viewRuleSetResponse.setId(ruleSet.getId());
        viewRuleSetResponse.setName(ruleSet.getName());
        viewRuleSetResponse.setCode(ruleSet.getCode());
        viewRuleSetResponse.setDescription(ruleSet.getDescription());
        viewRuleSetResponse.setWorkspaceId(ruleSet.getWorkspaceId());
        viewRuleSetResponse.setWorkspaceCode(ruleSet.getWorkspaceCode());
        viewRuleSetResponse.setStrategyType(ruleSet.getStrategyType().getValue());
        List<Rule> rules = ruleSet.getRules();
        List<RuleBody> ruleBodies = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);
            RuleBody ruleBody = this.getRuleBody(rule, i);
            ruleBodies.add(ruleBody);
        }
        viewRuleSetResponse.setRuleSet(ruleBodies);
        Rule defaultRule = ruleSet.getDefaultRule();
        if (defaultRule != null) {
            RuleBody ruleBody = this.getRuleBody(defaultRule, null);
            viewRuleSetResponse.setDefaultRule(ruleBody);
            viewRuleSetResponse.setEnableDefaultRule(EnableEnum.ENABLE.getStatus());
        } else {
            viewRuleSetResponse.setEnableDefaultRule(EnableEnum.DISABLE.getStatus());
        }
        // 规则set调用接口，以及规则set入参
        viewRuleSetResponse.setParameters(this.parameterService.getParameters(ruleSet));
        return viewRuleSetResponse;
    }

    private RuleBody getRuleBody(Rule defaultRule, Integer orderNo) {
        RuleBody ruleBody = new RuleBody();
        ruleBody.setId(defaultRule.getId());
        ruleBody.setName(defaultRule.getName());
        ruleBody.setOrderNo(orderNo);
        List<ConditionGroup> conditionGroups = defaultRule.getConditionSet().getConditionGroups();
        ruleBody.setConditionGroup(this.policyConditionGroupService.pressConditionGroupConfig(conditionGroups));
        ruleBody.setAction(valueResolve.getConfigValue(defaultRule.getActionValue()));
        return ruleBody;
    }


    /**
     * 删除规则集
     *
     * @param id 规则集id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(id);
        if (policyRuleSet == null) {
            return false;
        }
        // 从引擎中移除规则
        if (this.ruleSetEngine.isExists(policyRuleSet.getWorkspaceCode(), policyRuleSet.getCode())) {
            RuleSetMessageBody ruleSetMessageBody = new RuleSetMessageBody();
            ruleSetMessageBody.setType(RuleSetMessageBody.Type.REMOVE);
            ruleSetMessageBody.setWorkspaceId(policyRuleSet.getWorkspaceId());
            ruleSetMessageBody.setWorkspaceCode(policyRuleSet.getWorkspaceCode());
            ruleSetMessageBody.setRuleSetCode(policyRuleSet.getCode());
            this.eventPublisher.publishEvent(new RuleSetEvent(ruleSetMessageBody));
        }
        // 删除规则集规则
        this.deleteRuleSetRule(policyRuleSet);
        // 删除规则发布记录
        this.policyRuleSetPublishManager.lambdaUpdate().eq(PolicyRuleSetPublish::getRuleSetId, id).remove();
        // 删除规则
        return this.policyRuleSetManager.removeById(id);
    }

    /**
     * 规则集Code是否存在
     *
     * @param code 规则集code
     * @return true存在
     */
    @Override
    public Boolean ruleSetCodeIsExists(String code) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.policyRuleSetManager.lambdaQuery()
                .eq(PolicyRuleSet::getWorkspaceId, workspace.getId())
                .eq(PolicyRuleSet::getCode, code)
                .count();
        return count != null && count >= 1;
    }

}
