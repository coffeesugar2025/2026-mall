package com.policy.web.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.lang.Validator;
import com.policy.web.config.Context;
import com.policy.common.util.PageUtils;
import com.policy.common.vo.*;
import com.policy.web.vo.convert.BasicConversion;
import com.policy.web.vo.user.UserData;
import com.policy.web.vo.workspace.AccessKey;
import com.policy.web.vo.workspace.ListWorkspaceRequest;
import com.policy.web.vo.workspace.ListWorkspaceResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.policy.common.exception.ValidException;
import com.policy.web.service.WorkspaceService;
import com.policy.web.store.entity.PolicyWorkspace;
import com.policy.web.store.manager.PolicyWorkspaceManager;
import com.policy.web.store.mapper.PolicyWorkspaceMapper;
import com.policy.web.vo.workspace.Workspace;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @create 2020/11/21
 * @since 1.0.0
 */
@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Resource
    private PolicyWorkspaceManager policyWorkspaceManager;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PolicyWorkspaceMapper policyWorkspaceMapper;

    /**
     * 内存缓存
     */
    private final LRUCache<String, AccessKey> accessKeyCache = CacheUtil.newLRUCache(100);

    /**
     * 用户有权限的工作空间
     *
     * @param pageRequest 模糊查询参数
     * @return list
     */
    @Override
    public PageResult<ListWorkspaceResponse> list(PageRequest<ListWorkspaceRequest> pageRequest) {
        UserData userData = Context.getCurrentUser();
        Boolean isAdmin = userData.getIsAdmin();
        ListWorkspaceRequest query = pageRequest.getQuery();
        // 如果是管理员，有所有工作空间权限
        if (isAdmin) {
            return PageUtils.page(this.policyWorkspaceManager, pageRequest.getPage(), () -> {
                QueryWrapper<PolicyWorkspace> queryWrapper = new QueryWrapper<>();
                if (Validator.isNotEmpty(query.getCode())) {
                    queryWrapper.lambda().like(PolicyWorkspace::getCode, query.getCode());
                }
                if (Validator.isNotEmpty(query.getName())) {
                    queryWrapper.lambda().like(PolicyWorkspace::getName, query.getName());
                }
                PageUtils.defaultOrder(pageRequest.getOrders(), queryWrapper);
                return queryWrapper;
            }, BasicConversion.INSTANCE::convert);
        }
        Integer userId = userData.getId();
        PageBase page = pageRequest.getPage();
        Integer total = this.policyWorkspaceMapper.totalWorkspace(userId, query, page);
        if (total == null || total == 0) {
            return new PageResult<>();
        }
        List<ListWorkspaceResponse> listWorkspaceResponses = BasicConversion.INSTANCE.convert(this.policyWorkspaceMapper.listWorkspace(userId, query, page));
        PageResult<ListWorkspaceResponse> pageResult = new PageResult<>();
        pageResult.setData(new Rows<>(listWorkspaceResponses, new PageResponse(page.getPageIndex(), page.getPageSize(), total)));
        return pageResult;
    }

    /**
     * 普通用户是否有这个工作空间权限
     *
     * @param workspaceId 工作空间id
     * @param userId      用户id
     * @return true有权限
     */
    @Override
    public boolean hasWorkspacePermission(Integer workspaceId, Integer userId) {
        Integer count = this.policyWorkspaceMapper.countWorkspace(workspaceId, userId);
        return count != null && count > 0;
    }

    /**
     * 获取当前工作空间
     *
     * @return Workspace
     */
    @Override
    public Workspace currentWorkspace() {
        UserData userData = Context.getCurrentUser();
        RBucket<Workspace> bucket = this.redissonClient.getBucket(CURRENT_WORKSPACE + userData.getId());
        if (bucket.isExists()) {
            Workspace workspace = bucket.get();
            log.info("当前工作空间：" + workspace);
            return workspace;
        } else {
            PolicyWorkspace policyWorkspace = this.policyWorkspaceMapper.getFirstWorkspace();
            if (policyWorkspace != null) {
                Workspace workspace = new Workspace();
                workspace.setId(policyWorkspace.getId());
                workspace.setName(policyWorkspace.getName());
                workspace.setCode(policyWorkspace.getCode());
                bucket.set(workspace);
                return workspace;
            } else {
                throw new ValidException("没有可用工作空间");
            }
        }
    }

    /**
     * 切换工作空间
     *
     * @param id 工作空间id
     * @return true
     */
    @Override
    public Boolean change(Integer id) {
        PolicyWorkspace engineWorkspace = this.policyWorkspaceManager.getById(id);
        if (engineWorkspace == null) {
            throw new ValidException("找不到此工作空间：" + id);
        }
        UserData userData = Context.getCurrentUser();
        if (!userData.getIsAdmin()) {
            // 如果不是超级管理员，查看是否有此工作空间的工作空间权限
            if (!this.hasWorkspacePermission(id, userData.getId())) {
                throw new ValidException("你没有此工作空间权限");
            }
        }
        RBucket<Workspace> bucket = this.redissonClient.getBucket(CURRENT_WORKSPACE + userData.getId());
        Workspace workspace = new Workspace();
        workspace.setId(engineWorkspace.getId());
        workspace.setName(engineWorkspace.getName());
        workspace.setCode(engineWorkspace.getCode());
        bucket.set(workspace);
        return true;
    }


    /**
     * 根据工作空间code获取AccessKey
     *
     * @param code 工作空间code
     * @return AccessKey
     */
    @Override
    public AccessKey accessKey(String code) {
        AccessKey accessKey = this.accessKeyCache.get(code);
        if (accessKey != null) {
            return accessKey;
        }
        PolicyWorkspace engineWorkspace = this.policyWorkspaceManager.lambdaQuery()
                .eq(PolicyWorkspace::getCode, code).one();
        if (engineWorkspace == null) {
            throw new ValidException("找不到此工作空间：" + code);
        }
        accessKey = new AccessKey();
        accessKey.setId(engineWorkspace.getId());
        accessKey.setAccessKeyId(engineWorkspace.getAccessKeyId());
        accessKey.setAccessKeySecret(engineWorkspace.getAccessKeySecret());
        this.accessKeyCache.put(code, accessKey, 1000 * 60 * 60);
        return accessKey;
    }

}
