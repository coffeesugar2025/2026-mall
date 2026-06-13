package com.policy.web.service.impl;

import com.policy.web.store.entity.*;
import com.policy.web.store.manager.*;
import com.policy.common.annotation.DataPermission;
import com.policy.common.enums.Permission;
import com.policy.common.exception.DataPermissionException;
import com.policy.web.config.Context;
import com.policy.web.service.DataPermissionService;
import com.policy.web.service.WorkspaceService;
import com.policy.web.vo.user.UserData;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Objects;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @create 2020/12/13
 * @since 1.0.0
 */
@Service
public class DataPermissionServiceImpl implements DataPermissionService {

    @Resource
    private PolicyConditionManager policyConditionManager;
    @Resource
    private PolicyElementManager policyElementManager;
    @Resource
    private PolicyVariableManager policyVariableManager;
    @Resource
    private PolicyDecisionTableManager policyDecisionTableManager;
    @Resource
    private PolicyGeneralRuleManager policyGeneralRuleManager;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private PolicyRuleSetManager policyRuleSetManager;


    /**
     * 校验数据权限
     *
     * @param id             数据id
     * @param dataPermission dataPermission
     * @return true有权限
     */
    @Override
    public Boolean validDataPermission(Serializable id, DataPermission dataPermission) {
        Permission.DataType dataPermissionType = dataPermission.dataType();
        Permission.OperationType operationType = dataPermission.operationType();
        UserData userData = Context.getCurrentUser();
        Integer userId = userData.getId();
        switch (dataPermissionType) {
            case ELEMENT:
                PolicyElement policyElement = this.policyElementManager.getById(id);
                // 不影响后续逻辑
                if (policyElement == null) {
                    return true;
                }
                if (Objects.equals(policyElement.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyElement.getWorkspaceId(), operationType);
            case VARIABLE:
                PolicyVariable policyVariable = this.policyVariableManager.getById(id);
                // 不影响后续逻辑
                if (policyVariable == null) {
                    return true;
                }
                if (Objects.equals(policyVariable.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyVariable.getWorkspaceId(), operationType);
            case FUNCTION:
                break;
            case CONDITION:
                PolicyCondition policyCondition = this.policyConditionManager.getById(id);
                // 不影响后续逻辑
                if (policyCondition == null) {
                    return true;
                }
                if (Objects.equals(policyCondition.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyCondition.getWorkspaceId(), operationType);
            case GENERAL_RULE:
                PolicyGeneralRule policyGeneralRule = this.policyGeneralRuleManager.getById(id);
                // 不影响后续逻辑
                if (policyGeneralRule == null) {
                    return true;
                }
                if (Objects.equals(policyGeneralRule.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyGeneralRule.getWorkspaceId(), operationType);
            case DECISION_TABLE:
                PolicyDecisionTable policyDecisionTable = this.policyDecisionTableManager.getById(id);
                // 不影响后续逻辑
                if (policyDecisionTable == null) {
                    return true;
                }
                if (Objects.equals(policyDecisionTable.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyDecisionTable.getWorkspaceId(), operationType);
            case RULE_SET:
                PolicyRuleSet policyRuleSet = this.policyRuleSetManager.getById(id);
                // 不影响后续逻辑
                if (policyRuleSet == null) {
                    return true;
                }
                if (Objects.equals(policyRuleSet.getCreateUserId(), userId)) {
                    return true;
                }
                return this.permissionTypeProcess(userId, policyRuleSet.getWorkspaceId(), operationType);
            case ELEMENT_GROUP:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataPermissionType);
        }
        return true;
    }

    /**
     * 根据权限类型校验相应规则
     *
     * @param userId        用户id
     * @param workspaceId   工作空间id
     * @param operationType 操作类型
     * @return true有权限
     */
    private boolean permissionTypeProcess(Integer userId, Integer workspaceId, Permission.OperationType operationType) {
        switch (operationType) {
            case ADD:
                // 任意人都可以添加
                break;
            case DELETE:
                // 普通用户只能删除自己创建的数据，已经校验过了
                break;
            case UPDATE:
                // 普通用户只能更新自己创建的数据，已经校验过了
                break;
            case VALID_WORKSPACE:
                // 不可以查看别的空间的数据权限
                boolean workspacePermission = this.workspaceService.hasWorkspacePermission(workspaceId, userId);
                if (!workspacePermission) {
                    throw new DataPermissionException("你没有此工作空间的操作权限");
                }
                return true;
            case ADD_OR_UPDATE:
            case SELECT:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + operationType);
        }
        return false;
    }

}
