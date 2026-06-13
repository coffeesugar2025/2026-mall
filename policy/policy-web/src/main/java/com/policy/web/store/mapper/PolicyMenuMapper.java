package com.policy.web.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.policy.web.store.entity.PolicyMenu;
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
public interface PolicyMenuMapper extends BaseMapper<PolicyMenu> {


    /**
     * 根据用户id获取角色的菜单
     *
     * @param userId 用户id
     * @return BootMenu
     */
    List<PolicyMenu> listMenuByUserId(@Param("userId") Integer userId);

}
