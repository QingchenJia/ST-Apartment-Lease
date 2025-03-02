package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.SystemPost;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.SystemPostService;
import com.atguigu.lease.web.admin.service.SystemUserService;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【system_user(员工信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser> implements SystemUserService {
    @Resource
    private SystemPostService systemPostService;

    /**
     * 根据查询条件分页获取系统用户列表
     *
     * @param current 当前页码
     * @param size    每页记录数
     * @param queryVo 查询条件对象，包含用户姓名和电话
     * @return 返回包含系统用户项的分页对象
     */
    @Override
    public IPage<SystemUserItemVo> pageItem(long current, long size, SystemUserQueryVo queryVo) {
        // 创建分页对象并设置当前页码和每页记录数
        Page<SystemUser> page = new Page<>(current, size);

        // 获取查询条件中的用户姓名和电话
        String name = queryVo.getName();
        String phone = queryVo.getPhone();

        // 创建Lambda查询条件构建器
        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        // 根据用户姓名和电话进行查询，如果相应字段有值则添加查询条件
        queryWrapper.eq(StringUtils.hasText(name), SystemUser::getName, name)
                .eq(StringUtils.hasText(phone), SystemUser::getPhone, phone);

        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<SystemUser> systemUsers = page.getRecords();

        // 将查询结果转换为系统用户项VO列表，并在转换过程中设置岗位名称
        List<SystemUserItemVo> systemUserItemVos = systemUsers.stream()
                .map(systemUser -> {
                    SystemUserItemVo systemUserItemVo = new SystemUserItemVo();
                    BeanUtils.copyProperties(systemUser, systemUserItemVo);

                    // 根据用户对象中的岗位ID获取岗位信息，并设置到用户项VO中
                    SystemPost systemPost = systemPostService.getById(systemUser.getPostId());
                    systemUserItemVo.setPostName(systemPost.getName());

                    return systemUserItemVo;
                })
                .toList();

        // 创建结果分页对象，并从原分页对象中复制分页信息，但不包括记录列表
        Page<SystemUserItemVo> resultPage = new Page<>();
        BeanUtils.copyProperties(page, resultPage, "records");

        // 将转换后的用户项VO列表设置到结果分页对象中
        resultPage.setRecords(systemUserItemVos);

        // 返回结果分页对象
        return resultPage;
    }

    /**
     * 根据用户ID获取系统用户信息
     * <p>
     * 此方法首先创建一个SystemUserItemVo对象，然后通过用户ID获取SystemUser对象，并将属性复制到SystemUserItemVo中
     * 接着，根据用户对象中的岗位ID获取对应的SystemPost对象，如果该对象不为空，则将其名称设置到SystemUserItemVo中
     *
     * @param id 用户ID，用于查询系统用户
     * @return 返回包含用户信息的SystemUserItemVo对象
     */
    @Override
    public SystemUserItemVo getSystemUserById(Long id) {
        // 创建一个系统用户视图对象，用于封装用户信息
        SystemUserItemVo systemUserItemVo = new SystemUserItemVo();

        // 通过用户ID获取系统用户对象
        SystemUser systemUser = getById(id);
        // 将系统用户对象的属性复制到视图对象中
        BeanUtils.copyProperties(systemUser, systemUserItemVo);

        // 根据用户对象中的岗位ID获取对应的系统岗位对象
        SystemPost systemPost = systemPostService.getById(systemUser.getPostId());
        // 如果岗位对象不为空，则将其名称设置到视图对象中
        if (systemPost != null) {
            systemUserItemVo.setPostName(systemPost.getName());
        }

        // 返回填充好的视图对象
        return systemUserItemVo;
    }
}
