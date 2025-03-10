package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【view_appointment(预约看房信息表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface ViewAppointmentService extends IService<ViewAppointment> {
    List<AppointmentItemVo> listItem();

    AppointmentDetailVo getDetailById(Long id);
}
