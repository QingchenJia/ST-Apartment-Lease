package com.atguigu.lease.web.app.controller.appointment;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "看房预约信息")
@RestController
@RequestMapping("/app/appointment")
public class ViewAppointmentController {
    @Resource
    private ViewAppointmentService viewAppointmentService;

    @Operation(summary = "保存或更新看房预约")
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdate(@RequestBody ViewAppointment viewAppointment) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        viewAppointment.setUserId(loginUser.getId());

        viewAppointmentService.saveOrUpdate(viewAppointment);
        return Result.ok();
    }

    @Operation(summary = "查询个人预约看房列表")
    @GetMapping("/listItem")
    public Result<List<AppointmentItemVo>> listItem() {
        List<AppointmentItemVo> appointmentItemVos = viewAppointmentService.listItem();
        return Result.ok(appointmentItemVos);
    }

    @GetMapping("/getDetailById")
    @Operation(summary = "根据ID查询预约详情信息")
    public Result<AppointmentDetailVo> getDetailById(Long id) {
        AppointmentDetailVo appointmentDetailVo = viewAppointmentService.getDetailById(id);
        return Result.ok(appointmentDetailVo);
    }
}
