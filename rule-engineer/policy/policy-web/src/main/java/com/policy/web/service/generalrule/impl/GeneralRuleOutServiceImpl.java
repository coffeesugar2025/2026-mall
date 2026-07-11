package com.policy.web.service.generalrule.impl;


import com.policy.web.service.PolicyOutService;
import com.policy.web.vo.output.BatchExecuteRequest;
import com.policy.web.vo.output.ExecuteRequest;
import com.policy.web.vo.output.IsExistsRequest;
import com.policy.web.service.WorkspaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.policy.core.GeneralPolicy;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/22
 * @since 1.0.0
 */
@Primary
@Slf4j
@Service
public class GeneralRuleOutServiceImpl extends PolicyOutService {


    public GeneralRuleOutServiceImpl( GeneralPolicy generalpolicy, ThreadPoolTaskExecutor threadPoolTaskExecutor, WorkspaceService workspaceService) {
        super(generalpolicy, threadPoolTaskExecutor, workspaceService);
    }

    /**
     * 执行单个规则，获取执行结果
     *
     * @param executeRule 执行规则入参
     * @return 规则执行结果
     */
    @Override
    public Object execute(ExecuteRequest executeRule) {
        return super.execute(executeRule);
    }

    /**
     * 批量执行多个规则(一次最多2000个)，获取执行结果
     *
     * @param batchExecuteRequest 执行规则入参
     * @return 规则执行结果
     */
    @Override
    public Object batchExecute(BatchExecuteRequest batchExecuteRequest) {
        return super.batchExecute(batchExecuteRequest);
    }

    /**
     * 引擎中是否存在这个规则
     *
     * @param isExistsRequest 参数
     * @return true存在
     */
    @Override
    public Boolean isExists(IsExistsRequest isExistsRequest) {
        return super.isExists(isExistsRequest);
    }


}
