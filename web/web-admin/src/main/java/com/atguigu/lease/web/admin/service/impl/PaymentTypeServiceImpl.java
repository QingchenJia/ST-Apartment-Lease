package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.model.entity.RoomPaymentType;
import com.atguigu.lease.web.admin.mapper.PaymentTypeMapper;
import com.atguigu.lease.web.admin.service.PaymentTypeService;
import com.atguigu.lease.web.admin.service.RoomPaymentTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【payment_type(支付方式表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class PaymentTypeServiceImpl extends ServiceImpl<PaymentTypeMapper, PaymentType> implements PaymentTypeService {
    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    /**
     * 根据房间ID列出支付方式
     * <p>
     * 此方法通过房间ID查询与之关联的房间支付类型列表，并进一步获取这些支付类型的详细信息
     * 它首先构建一个查询条件以找到匹配的房间支付类型记录，然后提取这些记录关联的支付类型ID，
     * 最后根据这些ID列表查询并返回支付类型对象列表
     *
     * @param id 房间ID，用于查询与房间关联的支付方式
     * @return 返回与指定房间ID关联的支付方式列表如果找不到关联的支付方式，则返回null
     */
    @Override
    public List<PaymentType> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID相关的房间支付类型列表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);

        // 根据查询条件获取房间支付类型列表，并转换为PaymentType对象列表，设置到RoomDetailVo对象中
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(roomPaymentTypeQueryWrapper);
        if (CollectionUtils.isEmpty(roomPaymentTypes)) {
            return null;
        }

        // 提取房间支付类型的支付类型ID列表，用于后续查询具体的支付类型信息
        List<Long> paymentTypeIds = roomPaymentTypes.stream()
                .map(RoomPaymentType::getPaymentTypeId)
                .toList();

        // 根据支付类型ID列表查询并返回支付类型对象列表
        return listByIds(paymentTypeIds);
    }
}
