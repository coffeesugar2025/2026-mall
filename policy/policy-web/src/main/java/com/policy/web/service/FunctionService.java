package com.policy.web.service;

import com.policy.common.vo.PageRequest;
import com.policy.common.vo.PageResult;
import com.policy.web.vo.common.ExecuteTestRequest;
import com.policy.web.vo.function.GetFunctionResponse;
import com.policy.web.vo.function.ListFunctionRequest;
import com.policy.web.vo.function.ListFunctionResponse;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author
 * @date 2020/8/27
 * @since 1.0.0
 */
public interface FunctionService {

    /**
     * 函数列表
     *
     * @param pageRequest param
     * @return list
     */
    PageResult<ListFunctionResponse> list(PageRequest<ListFunctionRequest> pageRequest);

    /**
     * 查询函数详情
     *
     * @param id 函数id
     * @return 函数信息
     */
    GetFunctionResponse get(Integer id);

    /**
     * 函数模拟测试
     *
     * @param executeTestRequest 函数入参值
     * @return result
     */
    Object run(ExecuteTestRequest executeTestRequest);
}
