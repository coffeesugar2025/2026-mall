package com.policy.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.policy.web.config.Context;
import com.policy.web.service.ElementService;
import com.policy.web.store.entity.PolicyCondition;
import com.policy.web.store.entity.PolicyElement;
import com.policy.web.store.entity.PolicyFunctionValue;
import com.policy.web.vo.element.*;
import com.policy.common.util.PageUtils;
import com.policy.web.store.manager.PolicyConditionManager;
import com.policy.web.store.manager.PolicyElementManager;
import com.policy.web.store.manager.PolicyFunctionValueManager;
import com.policy.web.store.mapper.PolicyElementMapper;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.common.vo.*;
import com.policy.web.vo.user.UserData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.policy.common.exception.ValidException;
import com.policy.core.value.VariableType;
import com.policy.web.vo.workspace.Workspace;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/7/14
 * @since 1.0.0
 */
@Service
public class ElementServiceImpl implements ElementService {

    @Resource
    private PolicyElementManager policyElementManager;
    @Resource
    private PolicyFunctionValueManager policyFunctionValueManager;
    @Resource
    private PolicyConditionManager policyConditionManager;
    @Resource
    private PolicyElementMapper policyElementMapper;

    /**
     * 添加元素
     *
     * @param addConditionRequest 元素信息
     * @return true
     */
    @Override
    public Boolean add(AddElementRequest addConditionRequest) {
        if (this.elementCodeIsExists(addConditionRequest.getCode())) {
            throw new ValidException("元素Code：{}已经存在", addConditionRequest.getCode());
        }
        Workspace workspace = Context.getCurrentWorkspace();
        PolicyElement engineElement = new PolicyElement();
        UserData userData = Context.getCurrentUser();
        engineElement.setCreateUserId(userData.getId());
        engineElement.setCreateUserName(userData.getUsername());
        engineElement.setName(addConditionRequest.getName());
        engineElement.setCode(addConditionRequest.getCode());
        engineElement.setWorkspaceId(workspace.getId());
        engineElement.setDescription(addConditionRequest.getDescription());
        engineElement.setValueType(addConditionRequest.getValueType());
        return policyElementManager.save(engineElement);
    }

    /**
     * 元素code是否存在
     *
     * @param code 元素code
     * @return true存在
     */
    @Override
    public Boolean elementCodeIsExists(String code) {
        Workspace workspace = Context.getCurrentWorkspace();
        Integer count = this.policyElementManager.lambdaQuery()
                .eq(PolicyElement::getWorkspaceId, workspace.getId())
                .eq(PolicyElement::getCode, code)
                .count();
        return count != null && count >= 1;
    }

    /**
     * 元素列表
     *
     * @param pageRequest param
     * @return ListElementResponse
     */
    @Override
    public PageResult<ListElementResponse> list(PageRequest<ListElementRequest> pageRequest) {
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        PageBase page = pageRequest.getPage();
        Workspace workspace = Context.getCurrentWorkspace();
        return PageUtils.page(policyElementManager, page, () -> {
            QueryWrapper<PolicyElement> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(PolicyElement::getWorkspaceId, workspace.getId());
            PageUtils.defaultOrder(orders, wrapper);

            ListElementRequest query = pageRequest.getQuery();
            if (CollUtil.isNotEmpty(query.getValueType())) {
                wrapper.lambda().in(PolicyElement::getValueType, query.getValueType());
            }
            if (Validator.isNotEmpty(query.getName())) {
                wrapper.lambda().like(PolicyElement::getName, query.getName());
            }
            if (Validator.isNotEmpty(query.getCode())) {
                wrapper.lambda().like(PolicyElement::getCode, query.getCode());
            }
            return wrapper;
        }, m -> {
            ListElementResponse listElementResponse = new ListElementResponse();
            listElementResponse.setId(m.getId());
            listElementResponse.setName(m.getName());
            listElementResponse.setCode(m.getCode());

            listElementResponse.setValueType(m.getValueType());
            listElementResponse.setDescription(m.getDescription());
            listElementResponse.setCreateTime(m.getCreateTime());
            return listElementResponse;
        });
    }

    /**
     * 根据id查询元素
     *
     * @param id 元素id
     * @return GetElementResponse
     */
    @Override
    public GetElementResponse get(Integer id) {
        PolicyElement engineElement = this.policyElementManager.lambdaQuery()
                .eq(PolicyElement::getId, id)
                .one();
        return BasicConversion.INSTANCE.convert(engineElement);
    }

    /**
     * 根据元素id更新元素
     *
     * @param updateElementRequest 元素信息
     * @return true
     */
    @Override
    public Boolean update(UpdateElementRequest updateElementRequest) {
        PolicyElement engineElement = this.policyElementManager.lambdaQuery()
                .eq(PolicyElement::getId, updateElementRequest.getId())
                .one();
        if (engineElement == null) {
            throw new ValidException("找不到更新的元素");
        }
        engineElement.setId(updateElementRequest.getId());
        engineElement.setName(updateElementRequest.getName());
        engineElement.setDescription(updateElementRequest.getDescription());
        return this.policyElementManager.updateById(engineElement);
    }

    /**
     * 根据id删除元素
     * <p>
     * 发布规则或者决策表元素即使被删除也不影响引擎加载
     *
     * @param id 元素id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        {
            Integer count = this.policyFunctionValueManager.lambdaQuery()
                    .eq(PolicyFunctionValue::getType, VariableType.ELEMENT.getType())
                    .eq(PolicyFunctionValue::getValue, id).count();
            if (count != null && count > 0) {
                throw new ValidException("有函数值在引用此元素，无法删除");
            }
        }
        {
            Integer count = policyElementMapper.countPublishRuleElement(id);
            if (count != null && count > 0) {
                throw new ValidException("有发布/待发布规则在引用此元素，无法删除");
            }
        }
        {
            Integer count = policyElementMapper.countPublishRuleSetElement(id);
            if (count != null && count > 0) {
                throw new ValidException("有发布/待发布规则集在引用此元素，无法删除");
            }
        }
        {
            Integer count = policyElementMapper.countRuleElement(id);
            if (count != null && count > 0) {
                throw new ValidException("有规则在引用此元素，无法删除");
            }
        }
        {
            Integer count = policyElementMapper.countRuleSetElement(id);
            if (count != null && count > 0) {
                throw new ValidException("有规则集在引用此元素，无法删除");
            }
        }
        {
            int reference = this.policyElementMapper.countDecisionTableElementId(id);
            if (reference > 0) {
                throw new ValidException("有决策表在引用此元素，无法删除");
            }
        }
        {
            int reference = this.policyElementMapper.countPublishDecisionTableElementId(id);
            if (reference > 0) {
                throw new ValidException("有发布/待发布决策表在引用此元素，无法删除");
            }
        }
        {
            Integer count = policyConditionManager.lambdaQuery()
                    .and(a -> a.or(o -> o.eq(PolicyCondition::getLeftType, VariableType.ELEMENT.getType())
                            .eq(PolicyCondition::getLeftValue, id))

                            .or(o -> o.eq(PolicyCondition::getRightType, VariableType.ELEMENT.getType())
                                    .eq(PolicyCondition::getRightValue, id)
                            )).count();
            if (count != null && count > 0) {
                throw new ValidException("有条件在引用此元素，无法删除");
            }
        }
        return policyElementManager.removeById(id);
    }

}
