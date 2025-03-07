package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.ApartmentFeeValue;
import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.admin.mapper.FeeValueMapper;
import com.atguigu.lease.web.admin.service.ApartmentFeeValueService;
import com.atguigu.lease.web.admin.service.FeeKeyService;
import com.atguigu.lease.web.admin.service.FeeValueService;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
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
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue> implements FeeValueService {
    @Resource
    private ApartmentFeeValueService apartmentFeeValueService;

    @Resource
    private FeeKeyService feeKeyService;

    /**
     * 根据公寓ID查询费用信息列表
     *
     * @param id 公寓ID，用于查询与公寓关联的费用信息
     * @return 返回一个包含费用信息的List，如果公寓没有关联的费用信息，则返回null
     */
    @Override
    public List<FeeValueVo> listByApartmentId(Long id) {
        // 查询公寓的费用信息
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);

        // 如果查询结果为空，直接返回null
        if (CollectionUtils.isEmpty(apartmentFeeValues)) {
            return null;
        }

        // 将查询到的费用信息转换为FeeValueVo对象，并包含费用名称，便于后续处理和传输
        return apartmentFeeValues.stream()
                .map(apartmentFeeValue -> {
                    // 根据费用值ID获取费用值信息
                    FeeValue feeValue = getById(apartmentFeeValue.getFeeValueId());
                    // 根据费用键ID获取费用键信息，以便获取费用名称
                    FeeKey feeKey = feeKeyService.getById(feeValue.getFeeKeyId());

                    // 创建FeeValueVo对象，并设置费用名称
                    FeeValueVo feeValueVo = new FeeValueVo();
                    BeanUtils.copyProperties(feeValue, feeValueVo);
                    feeValueVo.setFeeKeyName(feeKey.getName());

                    return feeValueVo;
                })
                .toList();
    }
}
