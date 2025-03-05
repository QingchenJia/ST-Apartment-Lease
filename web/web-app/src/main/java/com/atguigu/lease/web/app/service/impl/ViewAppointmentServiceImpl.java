package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment> implements ViewAppointmentService {
    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private RoomInfoService roomInfoService;

    /**
     * 获取当前用户的所有预约项目列表
     * <p>
     * 此方法首先获取当前登录用户的信息，然后查询该用户的所有预约记录
     * 如果存在预约记录，则将这些记录转换为AppointmentItemVo对象，并添加额外的公寓信息和图表信息
     * 如果不存在预约记录，则返回null
     *
     * @return 当前用户的预约项目列表，如果列表为空则返回null
     */
    @Override
    public List<AppointmentItemVo> listItem() {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginUserHolder.getLoginUser();

        // 创建查询条件，筛选出当前用户的预约记录
        LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ViewAppointment::getUserId, loginUser.getId());

        // 执行查询，获取用户的预约记录
        List<ViewAppointment> viewAppointments = list(queryWrapper);

        // 检查预约记录是否为空，如果为空则返回null
        if (CollectionUtils.isEmpty(viewAppointments)) {
            return null;
        }

        // 将查询到的预约记录转换为AppointmentItemVo对象，并添加额外的信息
        return viewAppointments.stream()
                .map(viewAppointment -> {
                    // 创建一个新的AppointmentItemVo对象
                    AppointmentItemVo appointmentItemVo = new AppointmentItemVo();
                    // 将预约记录的属性复制到AppointmentItemVo对象中
                    BeanUtils.copyProperties(viewAppointment, appointmentItemVo);

                    // 根据预约记录中的公寓ID获取公寓信息，并设置到AppointmentItemVo对象中
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(viewAppointment.getApartmentId());
                    appointmentItemVo.setApartmentName(apartmentInfo.getName());

                    // 根据预约记录中的公寓ID获取所有相关的图表信息，并设置到AppointmentItemVo对象中
                    List<GraphVo> graphVos = graphInfoService.listByApartmentId(viewAppointment.getApartmentId());
                    appointmentItemVo.setGraphVoList(graphVos);

                    // 返回转换后的AppointmentItemVo对象
                    return appointmentItemVo;
                })
                .toList();
    }

    /**
     * 根据预约ID获取预约详情
     * <p>
     * 本方法实现预约详情的查询，通过预约ID获取相关信息，并组装成AppointmentDetailVo对象返回
     * 主要包括预约信息、公寓信息、标签信息、图片信息及房间信息的获取与处理
     *
     * @param id 预约ID，用于查询特定预约的详情
     * @return AppointmentDetailVo 返回预约详情对象，包含预约及相关公寓的详细信息
     */
    @Override
    public AppointmentDetailVo getDetailById(Long id) {
        // 初始化预约详情对象
        AppointmentDetailVo appointmentDetailVo = new AppointmentDetailVo();

        // 根据ID获取预约视图对象，并将属性复制到预约详情对象中
        ViewAppointment viewAppointment = getById(id);
        BeanUtils.copyProperties(viewAppointment, appointmentDetailVo);

        // 初始化公寓项目视图对象
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();

        // 根据预约中的公寓ID获取公寓信息，并将属性复制到公寓项目视图对象中
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(viewAppointment.getApartmentId());
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        // 获取与公寓ID关联的标签信息列表，并设置到公寓项目视图对象中
        List<LabelInfo> labelInfos = labelInfoService.listByApartmentId(viewAppointment.getApartmentId());
        apartmentItemVo.setLabelInfoList(labelInfos);

        // 获取与公寓ID关联的图片信息列表，并设置到公寓项目视图对象中
        List<GraphVo> graphVos = graphInfoService.listByApartmentId(viewAppointment.getApartmentId());
        apartmentItemVo.setGraphVoList(graphVos);

        // 创建查询条件，用于获取与公寓ID关联的房间信息列表
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, viewAppointment.getApartmentId());
        List<RoomInfo> roomInfos = roomInfoService.list(queryWrapper);

        // 如果房间信息列表非空，找到租金最低的房间，并设置其租金为公寓项目视图对象的最低租金
        if (!CollectionUtils.isEmpty(roomInfos)) {
            RoomInfo roomInfo = roomInfos.stream()
                    .min(Comparator.comparing(RoomInfo::getRent))
                    .get();

            apartmentItemVo.setMinRent(roomInfo.getRent());
        }

        // 将公寓项目视图对象设置到预约详情对象中
        appointmentDetailVo.setApartmentItemVo(apartmentItemVo);

        // 返回预约详情对象
        return appointmentDetailVo;
    }
}
