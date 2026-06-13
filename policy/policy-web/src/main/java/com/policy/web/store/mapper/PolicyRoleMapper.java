package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2020-09-24
 */
public interface PolicyRoleMapper extends BaseMapper<PolicyRole> {


    /**
     * 根据用户id查询此用户的所有角色
     *
     * @param userId 用户id
     * @return 角色列表
     */
    List<PolicyRole> listRoleByUserId(@Param("userId") Integer userId);

}
