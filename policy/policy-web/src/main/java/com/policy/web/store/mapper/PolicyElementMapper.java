package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.core.annotation.Param;
import com.policy.web.store.entity.PolicyElement;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author 
 * @since 2020-07-14
 */
public interface PolicyElementMapper extends BaseMapper<PolicyElement> {

    /**
     * 统计发布规则引用此元素
     *
     * @param id 元素id
     * @return int
     */
    Integer countPublishRuleElement(@Param("id") Integer id);

    /**
     * 统计发布规则集引用此元素
     *
     * @param id 元素id
     * @return int
     */
    Integer countPublishRuleSetElement(@Param("id") Integer id);

    /**
     * 统计规则引用此元素
     *
     * @param id 元素id
     * @return int
     */
    Integer countRuleElement(Integer id);

    /**
     * 统计规则集引用此元素
     *
     * @param id 元素id
     * @return int
     */
    Integer countRuleSetElement(Integer id);


    /**
     * 统计决策表引用此元素的数量
     *
     * @param id id
     * @return int
     */
    int countDecisionTableElementId(@Param("id") Integer id);

    /**
     * 统计发布决策表引用此元素的数量
     *
     * @param id id
     * @return int
     */
    int countPublishDecisionTableElementId(@Param("id") Integer id);
}
