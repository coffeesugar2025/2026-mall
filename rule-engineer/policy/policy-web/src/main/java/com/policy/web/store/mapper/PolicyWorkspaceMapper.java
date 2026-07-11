package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.common.vo.PageBase;
import com.policy.web.store.entity.PolicyWorkspace;
import com.policy.web.vo.workspace.ListWorkspaceRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 工作空间 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2020-11-21
 */
public interface PolicyWorkspaceMapper extends BaseMapper<PolicyWorkspace> {

    /**
     * 根据用户id获取用户有权限的工作空间
     *
     * @param userId 用户id
     * @return list
     */
    List<PolicyWorkspace> listWorkspaceByUserId(@Param("userId") Integer userId);

    /**
     * 是否存在此用户这个工作空间权限
     *
     * @param workspaceId 工作空间id
     * @param userId      用户id
     * @return count
     */
    Integer countWorkspace(@Param("workspaceId") Integer workspaceId, @Param("userId") Integer userId);

    /**
     * 根据用户id获取用户有权限的工作空间 带条件
     *
     * @param userId 用户id
     * @param query  条件
     * @param page   分页
     * @return policyWorkspace
     */
    List<PolicyWorkspace> listWorkspace(@Param("userId") Integer userId, @Param("query") ListWorkspaceRequest query, @Param("page") PageBase page);

    /**
     * 分页统计数量
     * <p>
     * 根据用户id获取用户有权限的工作空间 带条件
     *
     * @param userId 用户id
     * @param query  条件
     * @param page   分页
     * @return policyWorkspace
     */
    Integer totalWorkspace(@Param("userId") Integer userId, @Param("query") ListWorkspaceRequest query, @Param("page") PageBase page);

    /**
     * 获取第一条数据
     *
     * @return policyWorkspace
     */
    PolicyWorkspace getFirstWorkspace();
}
