package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.admin.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.admin.service.ApartmentInfoService;
import com.atguigu.lease.web.admin.service.ViewAppointmentService;
import com.atguigu.lease.web.admin.vo.appointment.AppointmentQueryVo;
import com.atguigu.lease.web.admin.vo.appointment.AppointmentVo;
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
 * &#064;description  针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment> implements ViewAppointmentService {
    @Resource
    private ApartmentInfoService apartmentInfoService;

    /**
     * 根据分页和查询条件获取预约信息列表
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param queryVo 查询条件对象，包含公寓ID、姓名和电话等查询字段
     * @return 返回一个分页对象，包含预约信息的列表
     */
    @Override
    public IPage<AppointmentVo> pageItem(long current, long size, AppointmentQueryVo queryVo) {
        // 创建分页对象，传入当前页码和每页大小
        Page<ViewAppointment> page = new Page<>(current, size);

        // 获取查询条件中的公寓ID、姓名和电话
        Long apartmentId = queryVo.getApartmentId();
        String name = queryVo.getName();
        String phone = queryVo.getPhone();

        // 创建Lambda查询条件构建器，用于构造动态查询条件
        LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
        // 根据公寓ID、姓名和电话构造查询条件，仅当这些字段有值时才添加到查询条件中
        queryWrapper.eq(apartmentId != null, ViewAppointment::getApartmentId, apartmentId)
                .eq(StringUtils.hasText(name), ViewAppointment::getName, name)
                .eq(StringUtils.hasText(phone), ViewAppointment::getPhone, phone);
        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<ViewAppointment> viewAppointments = page.getRecords();
        // 将查询结果转换为AppointmentVo对象列表，并关联公寓信息
        List<AppointmentVo> appointmentVos = viewAppointments.stream()
                .map(viewAppointment -> {
                    AppointmentVo appointmentVo = new AppointmentVo();
                    // 复制基础属性
                    BeanUtils.copyProperties(viewAppointment, appointmentVo);

                    // 根据公寓ID获取公寓信息，并设置到AppointmentVo对象中
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(viewAppointment.getApartmentId());
                    appointmentVo.setApartmentInfo(apartmentInfo);

                    return appointmentVo;
                })
                .toList();

        // 创建结果分页对象
        Page<AppointmentVo> resultPage = new Page<>();
        // 复制分页属性，但不包括记录列表
        BeanUtils.copyProperties(page, resultPage, "records");

        // 设置转换后的记录列表到结果分页对象
        resultPage.setRecords(appointmentVos);

        // 返回结果分页对象
        return resultPage;
    }
}
