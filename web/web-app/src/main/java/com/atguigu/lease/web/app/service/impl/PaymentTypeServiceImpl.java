package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.model.entity.RoomPaymentType;
import com.atguigu.lease.web.app.mapper.PaymentTypeMapper;
import com.atguigu.lease.web.app.service.PaymentTypeService;
import com.atguigu.lease.web.app.service.RoomPaymentTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【payment_type(支付方式表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class PaymentTypeServiceImpl extends ServiceImpl<PaymentTypeMapper, PaymentType> implements PaymentTypeService {
    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Override
    public List<PaymentType> listByRoomId(Long id) {
        LambdaQueryWrapper<RoomPaymentType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomPaymentType::getRoomId, id);

        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(queryWrapper);
        if (CollectionUtils.isEmpty(roomPaymentTypes)) {
            return null;
        }

        List<Long> paymentTypeIds = roomPaymentTypes.stream()
                .map(RoomPaymentType::getPaymentTypeId)
                .toList();

        return listByIds(paymentTypeIds);
    }
}
