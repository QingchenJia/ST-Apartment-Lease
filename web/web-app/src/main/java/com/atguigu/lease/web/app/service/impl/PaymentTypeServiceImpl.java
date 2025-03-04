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

    /**
     * 根据房间ID列出支付方式
     *
     * @param id 房间的唯一标识符
     * @return 对应房间的支付方式列表，如果房间没有关联的支付方式，则返回null
     */
    @Override
    public List<PaymentType> listByRoomId(Long id) {
        // 创建查询条件，用于查找与特定房间ID关联的所有支付方式
        LambdaQueryWrapper<RoomPaymentType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomPaymentType::getRoomId, id);

        // 执行查询，获取与房间ID关联的所有支付方式
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(queryWrapper);
        // 如果查询结果为空，直接返回null
        if (CollectionUtils.isEmpty(roomPaymentTypes)) {
            return null;
        }

        // 从查询结果中提取所有支付类型的ID
        List<Long> paymentTypeIds = roomPaymentTypes.stream()
                .map(RoomPaymentType::getPaymentTypeId)
                .toList();

        // 根据提取的支付类型ID列表，获取并返回对应的支付方式列表
        return listByIds(paymentTypeIds);
    }
}
