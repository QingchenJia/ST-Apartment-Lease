package com.atguigu.lease.web.app.controller.payment;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.web.app.service.PaymentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "支付方式接口")
@RestController
@RequestMapping("/app/payment")
public class PaymentTypeController {
    @Resource
    private PaymentTypeService paymentTypeService;

    @Operation(summary = "根据房间id获取可选支付方式列表")
    @GetMapping("/listByRoomId")
    public Result<List<PaymentType>> listByRoomId(@RequestParam Long id) {
        List<PaymentType> paymentTypes = paymentTypeService.listByRoomId(id);
        return Result.ok(paymentTypes);
    }

    @Operation(summary = "获取全部支付方式列表")
    @GetMapping("/list")
    public Result<List<PaymentType>> list() {
        List<PaymentType> paymentTypes = paymentTypeService.list();
        return Result.ok(paymentTypes);
    }
}
