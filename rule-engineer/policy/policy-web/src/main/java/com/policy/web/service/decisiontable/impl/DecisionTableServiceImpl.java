package com.policy.web.service.decisiontable.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.policy.web.listener.body.DecisionTableMessageBody;
import com.policy.web.listener.event.DecisionTableEvent;
import com.policy.web.vo.decisiontable.*;
import com.policy.web.vo.decisiontable.Rows;
import com.policy.common.VersionUtils;
import com.policy.core.DecisionTableEngine;
import com.policy.core.condition.Operator;
import com.policy.core.decisiontable.*;
import com.policy.common.exception.ValidException;
import com.policy.core.value.Value;
import com.policy.web.config.Context;
import com.policy.common.enums.DataStatus;
import com.policy.common.enums.EnableEnum;
import com.policy.web.service.ReferenceDataService;
import com.policy.web.service.ValueResolve;
import com.policy.web.service.decisiontable.DecisionTableService;
import com.policy.web.service.ParameterService;
import com.policy.web.store.entity.PolicyDecisionTable;
import com.policy.web.store.manager.PolicyDecisionTableManager;
import com.policy.web.store.manager.PolicyDecisionTablePublishManager;
import com.policy.web.store.entity.PolicyDecisionTablePublish;
import com.policy.common.util.PageUtils;
import com.policy.common.vo.*;
import com.policy.web.vo.common.ViewRequest;
import com.policy.web.vo.condition.ConfigValue;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.web.vo.generalrule.DefaultAction;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.workspace.Workspace;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/27
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class DecisionTableServiceImpl implements DecisionTableService {

    @Resource
    private PolicyDecisionTableManager policyDecisionTableManager;
    @Resource
    private PolicyDecisionTablePublishManager policyDecisionTablePublishManager;
    @Resource
    private DecisionTableEngine decisionTableEngine;
    @Resource
    private ApplicationEventPublisher eventPublisher;
    @Resource
    private ValueResolve valueResolve;
    @Resource
    private ParameterService parameterService;
    @Resource
    private ReferenceDataService referenceDataService;

    /**
     * 决策表列表
     *
     * @param pageRequest 分页查询参数
     * @return page
     */
    @Override
    public PageResult<ListDecisionTableResponse> list(PageRequest<ListDecisionTableRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        return PageUtils.page(this.policyDecisionTableManager, page, () -> {
            QueryWrapper<PolicyDecisionTable> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(PolicyDecisionTable::getWorkspaceId, workspace.getId());
            ListDecisionTableRequest query = pageRequest.getQuery();
            if (Validator.isNotEmpty(query.getName())) {
                wrapper.lambda().like(PolicyDecisionTable::getName, query.getName());
            }
            // 遗留bug修复
            if (Validator.isNotEmpty(query.getStatus())) {
                if (query.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
                    wrapper.lambda().isNotNull(PolicyDecisionTable::getPublishVersion);
                } else {
                    wrapper.lambda().eq(PolicyDecisionTable::getStatus, query.getStatus());
                }
            }
            if (Validator.isNotEmpty(query.getCode())) {
                wrapper.lambda().like(PolicyDecisionTable::getCode, query.getCode());
            }
            PageUtils.defaultOrder(orders, wrapper);
            return wrapper;
        }, m -> {
            ListDecisionTableResponse decisionTableResponse = new ListDecisionTableResponse();
            BeanUtil.copyProperties(m, decisionTableResponse);
            return decisionTableResponse;
        });
    }

    /**
     * 保存或者更新决策表定义信息
     *
     * @param decisionTableDefinition 定义信息
     * @return 决策表id
     */
    @Override
    public Integer saveOrUpdateDecisionTableDefinition(DecisionTableDefinition decisionTableDefinition) {
        PolicyDecisionTable policyDecisionTable = new PolicyDecisionTable();
        if (decisionTableDefinition.getId() == null) {
            if (this.decisionTableCodeIsExists(decisionTableDefinition.getCode())) {
                throw new ValidException("决策表Code：{}已经存在", decisionTableDefinition.getCode());
            }
            Workspace workspace = Context.getCurrentWorkspace();
            UserData userData = Context.getCurrentUser();
            policyDecisionTable.setCreateUserId(userData.getId());
            policyDecisionTable.setCreateUserName(userData.getUsername());
            policyDecisionTable.setWorkspaceId(workspace.getId());
            policyDecisionTable.setWorkspaceCode(workspace.getCode());
            // 初始化决策表
            TableData tableData = new TableData();
            tableData.getCollConditionHeads().add(new CollConditionHeads());
            Rows rows = new Rows();
            rows.getConditions().add(new ConfigValue());
            tableData.getRows().add(rows);
            policyDecisionTable.setTableData(JSON.toJSONString(tableData));
            // 默认执行策略
            policyDecisionTable.setStrategyType(DecisionTableStrategyType.ALL_PRIORITY.getValue());
        } else {
            policyDecisionTable = this.policyDecisionTableManager.lambdaQuery()
                    .eq(PolicyDecisionTable::getId, decisionTableDefinition.getId())
                    .one();
            if (policyDecisionTable == null) {
                throw new ValidException("不存在决策表:{}", decisionTableDefinition.getId());
            } else {
                if (Objects.equals(policyDecisionTable.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
                    // 删除原有待发布规则
                    this.policyDecisionTablePublishManager.lambdaUpdate()
                            .eq(PolicyDecisionTablePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                            .eq(PolicyDecisionTablePublish::getDecisionTableId, policyDecisionTable.getId())
                            .remove();
                }
            }
        }
        policyDecisionTable.setId(decisionTableDefinition.getId());
        policyDecisionTable.setName(decisionTableDefinition.getName());
        policyDecisionTable.setCode(decisionTableDefinition.getCode());
        policyDecisionTable.setDescription(decisionTableDefinition.getDescription());
        policyDecisionTable.setStatus(DataStatus.EDIT.getStatus());
        this.policyDecisionTableManager.saveOrUpdate(policyDecisionTable);
        return policyDecisionTable.getId();
    }

    /**
     * 决策表code是否存在
     *
     * @param code 规则code
     * @return true存在
     */
    @Override
    public Boolean decisionTableCodeIsExists(String code) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.policyDecisionTableManager.lambdaQuery()
                .eq(PolicyDecisionTable::getWorkspaceId, workspace.getId())
                .eq(PolicyDecisionTable::getCode, code)
                .count();
        return count != null && count >= 1;
    }

    /**
     * 查询决策表定义信息
     *
     * @param id 决策表id
     * @return DecisionTableDefinition
     */
    @Override
    public DecisionTableDefinition getDecisionTableDefinition(Integer id) {
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.lambdaQuery()
                .eq(PolicyDecisionTable::getId, id)
                .one();
        if (policyDecisionTable == null) {
            return null;
        }
        return BasicConversion.INSTANCE.convert(policyDecisionTable);
    }

    /**
     * 删除决策表
     *
     * @param id 决策表id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(id);
        if (policyDecisionTable == null) {
            return false;
        }
        // 从引擎中移除规则
        if (this.decisionTableEngine.isExists(policyDecisionTable.getWorkspaceCode(), policyDecisionTable.getCode())) {
            DecisionTableMessageBody decisionTableMessageBody = new DecisionTableMessageBody();
            decisionTableMessageBody.setType(DecisionTableMessageBody.Type.REMOVE);
            decisionTableMessageBody.setWorkspaceId(policyDecisionTable.getWorkspaceId());
            decisionTableMessageBody.setWorkspaceCode(policyDecisionTable.getWorkspaceCode());
            decisionTableMessageBody.setDecisionTableCode(policyDecisionTable.getCode());
            this.eventPublisher.publishEvent(new DecisionTableEvent(decisionTableMessageBody));
        }
        this.policyDecisionTablePublishManager.lambdaUpdate().eq(PolicyDecisionTablePublish::getDecisionTableId, id).remove();
        return this.policyDecisionTableManager.removeById(id);
    }

    /**
     * 更新决策表信息
     *
     * @param updateDecisionTableRequest 决策表配置数据
     * @return true执行成功
     */
    @Override
    public Boolean updateDecisionTable(UpdateDecisionTableRequest updateDecisionTableRequest) {
        Integer decisionTableId = updateDecisionTableRequest.getId();
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(decisionTableId);
        if (policyDecisionTable == null) {
            throw new ValidException("不存在决策表:{}", decisionTableId);
        }
        if (Objects.equals(policyDecisionTable.getStatus(), DataStatus.WAIT_PUBLISH.getStatus())) {
            this.policyDecisionTablePublishManager.lambdaUpdate()
                    .eq(PolicyDecisionTablePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                    .eq(PolicyDecisionTablePublish::getDecisionTableId, decisionTableId)
                    .remove();
        }
        policyDecisionTable.setStrategyType(updateDecisionTableRequest.getStrategyType());
        policyDecisionTable.setStatus(DataStatus.EDIT.getStatus());
        policyDecisionTable.setTableData(JSON.toJSONString(updateDecisionTableRequest.getTableData()));
        policyDecisionTable.setReferenceData(JSON.toJSONString(referenceDataService.countReferenceData(updateDecisionTableRequest.getTableData())));
        return this.policyDecisionTableManager.updateById(policyDecisionTable);
    }

    /**
     * 获取决策表信息
     *
     * @param id 决策表id
     * @return 决策表信息
     */
    @Override
    public GetDecisionTableResponse getDecisionTableConfig(Integer id) {
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(id);
        if (policyDecisionTable == null) {
            return null;
        }
        GetDecisionTableResponse decisionTableResponse = new GetDecisionTableResponse();
        decisionTableResponse.setId(id);
        decisionTableResponse.setName(policyDecisionTable.getName());
        decisionTableResponse.setCode(policyDecisionTable.getCode());
        decisionTableResponse.setDescription(policyDecisionTable.getDescription());
        decisionTableResponse.setWorkspaceId(policyDecisionTable.getWorkspaceId());
        decisionTableResponse.setWorkspaceCode(policyDecisionTable.getWorkspaceCode());
        decisionTableResponse.setTableData(JSON.parseObject(policyDecisionTable.getTableData(), TableData.class));
        decisionTableResponse.setStrategyType(policyDecisionTable.getStrategyType());
        return decisionTableResponse;
    }

    /**
     * 生成决策表代发布
     *
     * @param releaseRequest 配置数据
     * @return true
     */
    @Override
    public Boolean generationRelease(GenerationReleaseRequest releaseRequest) {
        // 校验是否配置完善
        DefaultAction defaultAction = releaseRequest.getTableData().getCollResultHead().getDefaultAction();
        defaultAction.valid();
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(releaseRequest.getId());
        if (policyDecisionTable == null) {
            throw new ValidException("不存在决策表:{}", releaseRequest.getId());
        }
        // 更新版本
        if (StrUtil.isBlank(policyDecisionTable.getCurrentVersion())) {
            policyDecisionTable.setCurrentVersion(VersionUtils.INIT_VERSION);
        } else {
            // 如果待发布与已经发布版本一致，则需要更新一个版本号
            if (policyDecisionTable.getCurrentVersion().equals(policyDecisionTable.getPublishVersion())) {
                // 获取下一个版本
                policyDecisionTable.setCurrentVersion(VersionUtils.getNextVersion(policyDecisionTable.getCurrentVersion()));
            }
        }
        policyDecisionTable.setStatus(DataStatus.WAIT_PUBLISH.getStatus());
        policyDecisionTable.setStrategyType(releaseRequest.getStrategyType());
        policyDecisionTable.setTableData(JSON.toJSONString(releaseRequest.getTableData()));
        String referenceDataJson = JSON.toJSONString(referenceDataService.countReferenceData(releaseRequest.getTableData()));
        policyDecisionTable.setReferenceData(referenceDataJson);
        this.policyDecisionTableManager.updateById(policyDecisionTable);
        // 删除原有待发布规则
        this.policyDecisionTablePublishManager.lambdaUpdate()
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableId, policyDecisionTable.getId())
                .remove();
        // 生成新的待发布
        PolicyDecisionTablePublish policyDecisionTablePublish = new PolicyDecisionTablePublish();
        policyDecisionTablePublish.setDecisionTableId(policyDecisionTable.getId());
        policyDecisionTablePublish.setDecisionTableCode(policyDecisionTable.getCode());
        policyDecisionTablePublish.setWorkspaceId(policyDecisionTable.getWorkspaceId());
        policyDecisionTablePublish.setWorkspaceCode(policyDecisionTable.getWorkspaceCode());
        DecisionTable decisionTable = this.decisionTableProcess(policyDecisionTable);
        policyDecisionTablePublish.setData(decisionTable.toJson());
        policyDecisionTablePublish.setStatus(policyDecisionTable.getStatus());
        policyDecisionTablePublish.setReferenceData(referenceDataJson);
        // add version
        policyDecisionTablePublish.setVersion(policyDecisionTable.getCurrentVersion());
        this.policyDecisionTablePublishManager.save(policyDecisionTablePublish);
        return true;
    }

    /**
     * 处理引擎决策表
     */
    public DecisionTable decisionTableProcess(PolicyDecisionTable policyDecisionTable) {
        TableData tableData = JSON.parseObject(policyDecisionTable.getTableData(), TableData.class);
        DecisionTable decisionTable = new DecisionTable();
        decisionTable.setId(policyDecisionTable.getId());
        decisionTable.setCode(policyDecisionTable.getCode());
        decisionTable.setName(policyDecisionTable.getName());
        decisionTable.setDescription(policyDecisionTable.getDescription());
        decisionTable.setWorkspaceId(policyDecisionTable.getWorkspaceId());
        decisionTable.setWorkspaceCode(policyDecisionTable.getWorkspaceCode());
        decisionTable.setStrategyType(DecisionTableStrategyType.getByValue(policyDecisionTable.getStrategyType()));
        List<CollConditionHeads> collConditionHeads = tableData.getCollConditionHeads();
        for (CollConditionHeads collConditionHead : collConditionHeads) {
            CollHead collHead = new CollHead();
            ConfigValue leftValue = collConditionHead.getLeftValue();
            collHead.setName(collConditionHead.getName());
            collHead.setLeftValue(this.valueResolve.getValue(leftValue.getType(), leftValue.getValueType(), leftValue.getValue()));
            collHead.setOperator(Operator.getByName(collConditionHead.getSymbol()));
            decisionTable.addCollHead(collHead);
        }
        // 首先过滤掉没有结果的
        List<Rows> rows = tableData.getRows().stream().filter(f -> {
            ConfigValue result = f.getResult();
            if (result.getType() == null) {
                return false;
            }
            if (StrUtil.isBlank(result.getValue())) {
                return false;
            }
            return !StrUtil.isBlank(result.getValueType());
        }).collect(Collectors.toList());
        for (int i = 0; i < rows.size(); i++) {
            Rows row = rows.get(i);
            Row decisionTableRow = new Row();
            decisionTableRow.setOrder(i);
            decisionTableRow.setPriority(row.getPriority());
            List<ConfigValue> conditions = row.getConditions();
            for (ConfigValue condition : conditions) {
                if (condition.getType() == null) {
                    // 空的单元格
                    decisionTableRow.addColl(new Coll());
                } else {
                    Value value = this.valueResolve.getValue(condition.getType(), condition.getValueType(), condition.getValue());
                    decisionTableRow.addColl(new Coll(value));
                }
            }
            ConfigValue result = row.getResult();
            Value value = this.valueResolve.getValue(result.getType(), result.getValueType(), result.getValue());
            decisionTableRow.setAction(value);
            decisionTable.addRow(decisionTableRow);
        }
        DefaultAction defaultAction = tableData.getCollResultHead().getDefaultAction();
        if (EnableEnum.ENABLE.getStatus().equals(defaultAction.getEnableDefaultAction())) {
            decisionTable.setDefaultActionValue(this.valueResolve.getValue(defaultAction.getType(), defaultAction.getValueType(), defaultAction.getValue()));
        }
        return decisionTable;
    }


    /**
     * 获取决策表展示信息
     *
     * @param viewRequest 决策表id
     * @return ViewDecisionTableResponse
     */
    @Override
    public ViewDecisionTableResponse view(ViewRequest viewRequest) {
        Integer id = viewRequest.getId();
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(id);
        if (policyDecisionTable == null) {
            throw new ValidException("找不到预览的规则数据:{}", id);
        }
        // 如果只有已发布
        if (policyDecisionTable.getStatus().equals(DataStatus.PUBLISHED.getStatus()) || viewRequest.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            PolicyDecisionTablePublish policyDecisionTablePublish = this.policyDecisionTablePublishManager.lambdaQuery()
                    .eq(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                    .eq(PolicyDecisionTablePublish::getDecisionTableId, id)
                    .one();
            if (policyDecisionTablePublish == null) {
                throw new ValidException("找不到发布的规则:{}", id);
            }
            String data = policyDecisionTablePublish.getData();
            DecisionTable decisionTable = DecisionTable.buildDecisionTable(data);
            return this.decisionTableResponseProcess(decisionTable);
        }
        PolicyDecisionTablePublish policyDecisionTablePublish = this.policyDecisionTablePublishManager.lambdaQuery()
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableId, id)
                .one();
        if (policyDecisionTablePublish == null) {
            throw new ValidException("找不到预览的规则数据:{}", id);
        }
        String data = policyDecisionTablePublish.getData();
        DecisionTable decisionTable = DecisionTable.buildDecisionTable(data);
        return this.decisionTableResponseProcess(decisionTable);
    }

    /**
     * 规则决策表
     *
     * @param id 决策表id
     * @return true
     */
    @Override
    public Boolean publish(Integer id) {
        PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(id);
        if (policyDecisionTable == null) {
            throw new ValidException("不存在决策表:{}", id);
        }
        if (policyDecisionTable.getStatus().equals(DataStatus.EDIT.getStatus())) {
            throw new ValidException("该决策表不可执行:{}", id);
        }
        // 如果已经是发布决策表了
        if (policyDecisionTable.getStatus().equals(DataStatus.PUBLISHED.getStatus())) {
            return true;
        }
        // 修改为已发布
        this.policyDecisionTableManager.lambdaUpdate()
                .set(PolicyDecisionTable::getStatus, DataStatus.PUBLISHED.getStatus())
                // 更新发布的版本号
                .set(PolicyDecisionTable::getPublishVersion, policyDecisionTable.getCurrentVersion())
                .eq(PolicyDecisionTable::getId, policyDecisionTable.getId())
                .update();
        /*
         * 删除原有的已发布决策表数据
         * 修改为，原有已发布为历史版本，后期准备回退
         */
        this.policyDecisionTablePublishManager.lambdaUpdate()
                .set(PolicyDecisionTablePublish::getStatus, DataStatus.HISTORY_PUBLISHED.getStatus())
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableId, policyDecisionTable.getId())
                .update();
        // 更新待发布为已发布
        this.policyDecisionTablePublishManager.lambdaUpdate()
                .set(PolicyDecisionTablePublish::getStatus, DataStatus.PUBLISHED.getStatus())
                .eq(PolicyDecisionTablePublish::getStatus, DataStatus.WAIT_PUBLISH.getStatus())
                .eq(PolicyDecisionTablePublish::getDecisionTableId, policyDecisionTable.getId())
                .update();
        // 加载规则
        DecisionTableMessageBody decisionTableMessageBody = new DecisionTableMessageBody();
        decisionTableMessageBody.setType(DecisionTableMessageBody.Type.LOAD);
        decisionTableMessageBody.setDecisionTableCode(policyDecisionTable.getCode());
        decisionTableMessageBody.setWorkspaceId(policyDecisionTable.getWorkspaceId());
        decisionTableMessageBody.setWorkspaceCode(policyDecisionTable.getWorkspaceCode());
        this.eventPublisher.publishEvent(new DecisionTableEvent(decisionTableMessageBody));
        return true;
    }

    /**
     * 处理决策表预览数据
     *
     * @param decisionTable 决策表
     * @return ViewDecisionTableResponse
     */
    private ViewDecisionTableResponse decisionTableResponseProcess(DecisionTable decisionTable) {
        ViewDecisionTableResponse decisionTableResponse = new ViewDecisionTableResponse();
        decisionTableResponse.setId(decisionTable.getId());
        decisionTableResponse.setName(decisionTable.getName());
        decisionTableResponse.setCode(decisionTable.getCode());
        decisionTableResponse.setDescription(decisionTable.getDescription());
        decisionTableResponse.setStrategyType(decisionTable.getStrategyType().getValue());
        decisionTableResponse.setWorkspaceId(decisionTable.getWorkspaceId());
        decisionTableResponse.setWorkspaceCode(decisionTable.getWorkspaceCode());
        TableData tableData = new TableData();
        List<CollHead> collHeads = decisionTable.getCollHeads();
        List<CollConditionHeads> collConditionHeads = new ArrayList<>();
        for (CollHead collHead : collHeads) {
            CollConditionHeads conditionHeads = new CollConditionHeads();
            conditionHeads.setName(collHead.getName());
            conditionHeads.setSymbol(collHead.getOperator().name());
            conditionHeads.setLeftValue(this.valueResolve.getConfigValue(collHead.getLeftValue()));
            collConditionHeads.add(conditionHeads);
        }
        tableData.setCollConditionHeads(collConditionHeads);
        Map<Integer, List<Row>> decisionTree = decisionTable.getDecisionTree();
        List<Rows> rows = new ArrayList<>();
        decisionTree.values().stream().flatMap(Collection::stream).sorted(Comparator.comparing(Row::getOrder)).forEach(f -> {
            Rows row = new Rows();
            row.setPriority(f.getPriority());
            List<Coll> colls = f.getColls();
            List<ConfigValue> conditions = new ArrayList<>();
            for (Coll coll : colls) {
                // 空的单元格
                if (coll.getRightValue() == null) {
                    conditions.add(new ConfigValue());
                } else {
                    ConfigValue configValue = this.valueResolve.getConfigValue(coll.getRightValue());
                    conditions.add(configValue);
                }
            }
            row.setConditions(conditions);
            ConfigValue result = this.valueResolve.getConfigValue(f.getAction());
            row.setResult(result);
            rows.add(row);
        });
        tableData.setRows(rows);
        CollResultHead collResultHead = new CollResultHead();
        Value defaultActionValue = decisionTable.getDefaultActionValue();
        DefaultAction defaultAction;
        if (defaultActionValue != null) {
            ConfigValue configValue = this.valueResolve.getConfigValue(defaultActionValue);
            defaultAction = new DefaultAction(configValue);
        } else {
            defaultAction = new DefaultAction();
        }
        collResultHead.setDefaultAction(defaultAction);
        tableData.setCollResultHead(collResultHead);
        decisionTableResponse.setTableData(tableData);
        decisionTableResponse.setParameters(this.parameterService.getParameters(decisionTable));
        return decisionTableResponse;
    }

}
