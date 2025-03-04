package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentFeeValue;
import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.app.mapper.FeeValueMapper;
import com.atguigu.lease.web.app.service.ApartmentFeeValueService;
import com.atguigu.lease.web.app.service.FeeKeyService;
import com.atguigu.lease.web.app.service.FeeValueService;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【fee_value(杂项费用值表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue> implements FeeValueService {
    @Resource
    private ApartmentFeeValueService apartmentFeeValueService;

    @Resource
    private FeeKeyService feeKeyService;

    /**
     * 根据公寓ID列出费用值
     *
     * @param id 公寓ID
     * @return 费用值列表，如果找不到则返回null
     */
    @Override
    public List<FeeValueVo> listByApartmentId(Long id) {
        // 创建查询条件，筛选具有指定公寓ID的费用值
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);

        // 执行查询并检查结果是否为空
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);
        if (CollectionUtils.isEmpty(apartmentFeeValues)) {
            return null;
        }

        // 将查询到的公寓费用值转换为费用值视图对象列表
        return apartmentFeeValues.stream().map(apartmentFeeValue -> {
            FeeValueVo feeValueVo = new FeeValueVo();

            // 根据费用值ID获取费用值详细信息，并复制属性到视图对象
            FeeValue feeValue = getById(apartmentFeeValue.getFeeValueId());
            BeanUtils.copyProperties(feeValue, feeValueVo);

            // 获取费用项名称并设置到视图对象
            FeeKey feeKey = feeKeyService.getById(feeValue.getFeeKeyId());
            feeValueVo.setFeeKeyName(feeKey.getName());

            return feeValueVo;
        }).toList();
    }
}
