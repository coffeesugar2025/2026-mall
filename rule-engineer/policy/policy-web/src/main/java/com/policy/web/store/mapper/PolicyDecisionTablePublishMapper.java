package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyDecisionTablePublish;
import org.apache.ibatis.annotations.Param;


public interface PolicyDecisionTablePublishMapper extends BaseMapper<PolicyDecisionTablePublish> {

    /**
     * 统计决策表引用此变量的数量
     *
     * @param id id
     * @return int
     */
    int countReferenceByVariableId(@Param("id") Integer id);

    /**
     * 统计决策表引用此元素的数量
     *
     * @param id id
     * @return int
     */
    int countReferenceByElementId(@Param("id") Integer id);

}
