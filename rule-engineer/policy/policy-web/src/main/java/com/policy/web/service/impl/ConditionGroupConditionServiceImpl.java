package com.policy.web.service.impl;

import com.policy.web.service.ConditionGroupConditionService;
import com.policy.web.store.manager.PolicyConditionGroupConditionManager;
import com.policy.web.vo.condition.group.condition.SaveOrUpdateConditionGroupCondition;
import com.policy.web.vo.convert.BasicConversion;
import org.springframework.stereotype.Service;
import com.policy.web.store.entity.PolicyConditionGroupCondition;
import javax.annotation.Resource;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/28
 * @since 1.0.0
 */
@Service
public class ConditionGroupConditionServiceImpl implements ConditionGroupConditionService {

    @Resource
    private PolicyConditionGroupConditionManager policyConditionGroupConditionManager;

    /**
     * 保存或者更新条件组条件
     *
     * @param saveOrUpdateConditionGroup 条件组条件信息
     * @return int
     */
    @Override
    public Integer saveOrUpdateConditionGroupCondition(SaveOrUpdateConditionGroupCondition saveOrUpdateConditionGroup) {
        PolicyConditionGroupCondition groupCondition = BasicConversion.INSTANCE.convert(saveOrUpdateConditionGroup);
        this.policyConditionGroupConditionManager.saveOrUpdate(groupCondition);
        return groupCondition.getId();
    }

    /**
     * 删除条件组条件
     *
     * @param id 条件组条件id
     * @return true
     */
    @Override
    public Boolean delete(Integer id) {
        return this.policyConditionGroupConditionManager.removeById(id);
    }

}
