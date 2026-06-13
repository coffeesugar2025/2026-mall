package com.policy.web.vo.convert;

import com.policy.web.store.entity.*;
import com.policy.web.vo.condition.ConfigValue;
import com.policy.web.vo.condition.group.condition.SaveOrUpdateConditionGroupCondition;
import com.policy.web.vo.decisiontable.DecisionTableDefinition;
import com.policy.web.vo.element.GetElementResponse;
import com.policy.web.vo.generalrule.DefaultAction;
import com.policy.web.vo.generalrule.GeneralRuleDefinition;
import com.policy.web.vo.ruleset.RuleSetDefinition;
import com.policy.web.vo.variable.GetVariableResponse;
import com.policy.web.vo.workspace.ListWorkspaceResponse;
import com.policy.web.store.entity.*;
import com.policy.web.vo.menu.ListMenuResponse;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/12/12
 * @since 1.0.0
 */
@Mapper
public interface BasicConversion {

    BasicConversion INSTANCE = Mappers.getMapper(BasicConversion.class);

    /**
     * policyMenu to ListMenuResponse
     *
     * @param policyMenu policyMenu
     * @return ListMenuResponse
     */
    ListMenuResponse convert(PolicyMenu policyMenu);

    /**
     * policyElement to GetElementResponse
     *
     * @param policyElement policyElement
     * @return GetElementResponse
     */
    GetElementResponse convert(PolicyElement policyElement);

    /**
     * policyVariable to GetVariableResponse
     *
     * @param policyVariable policyVariable
     * @return GetVariableResponse
     */
    GetVariableResponse convert(PolicyVariable policyVariable);

    /**
     * policyRule to RuleDefinition
     *
     * @param policyRule policyRule
     * @return RuleDefinition
     */
    GeneralRuleDefinition convert(PolicyRule policyRule);

    /**
     * value to DefaultAction
     *
     * @param value value
     * @return DefaultAction
     */
    DefaultAction convert(ConfigValue value);

    /**
     * policyUser to UserData
     *
     * @param policyUser policyUser
     * @return UserData
     */
    UserData convert(PolicyUser policyUser);

    /**
     * userData to UserResponse
     *
     * @param userData userData
     * @return UserResponse
     */
    UserResponse convert(UserData userData);

    /**
     * saveOrUpdateConditionGroup to  policyConditionGroupCondition
     *
     * @param saveOrUpdateConditionGroup saveOrUpdateConditionGroup
     * @return policyConditionGroupCondition
     */
    PolicyConditionGroupCondition convert(SaveOrUpdateConditionGroupCondition saveOrUpdateConditionGroup);

    /**
     * policyWorkspaces to ListWorkspaceResponse
     *
     * @param policyWorkspaces policyWorkspaces
     * @return ListWorkspaceResponse
     */
    List<ListWorkspaceResponse> convert(List<PolicyWorkspace> policyWorkspaces);

    /**
     * policyWorkspace to ListWorkspaceResponse
     *
     * @param policyWorkspace policyWorkspace
     * @return ListWorkspaceResponse
     */
    ListWorkspaceResponse convert(PolicyWorkspace policyWorkspace);

    /**
     * policyDecisionTable to DecisionTableDefinition
     *
     * @param policyDecisionTable policyDecisionTable
     * @return DecisionTableDefinition
     */
    DecisionTableDefinition convert(PolicyDecisionTable policyDecisionTable);

    /**
     * policyRuleSet to RuleSetDefinition
     *
     * @param policyRuleSet policyRuleSet
     * @return RuleSetDefinition
     */
    RuleSetDefinition convert(PolicyRuleSet policyRuleSet);

    /**
     * engineGeneralRule to GeneralRuleDefinition
     *
     * @param engineGeneralRule engineGeneralRule
     * @return GeneralRuleDefinition
     */
    GeneralRuleDefinition convert(PolicyGeneralRule engineGeneralRule);

}
