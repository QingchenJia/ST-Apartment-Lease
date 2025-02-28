package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.admin.mapper.FeeKeyMapper;
import com.atguigu.lease.web.admin.service.FeeKeyService;
import com.atguigu.lease.web.admin.service.FeeValueService;
import com.atguigu.lease.web.admin.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【fee_key(杂项费用名称表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class FeeKeyServiceImpl extends ServiceImpl<FeeKeyMapper, FeeKey> implements FeeKeyService {
    @Resource
    private FeeValueService feeValueService;

    /**
     * 获取费用信息列表
     * <p>
     * 此方法用于查询并构建所有费用项目的详细信息，包括费用项目的键信息和对应的值信息
     * 它首先查询所有费用键，然后遍历每个费用键，查询与之关联的费用值列表，
     * 并将这些信息封装到FeeKeyVo对象中，最终返回一个包含所有这些信息的列表
     *
     * @return 包含所有费用键和费用值信息的列表
     */
    @Override
    public List<FeeKeyVo> feeInfoList() {
        // 初始化费用信息列表
        List<FeeKeyVo> feeKeyVos = new ArrayList<>();

        // 查询所有费用键
        List<FeeKey> feeKeys = list();

        // 遍历每个费用键，查询并封装对应的费用值信息
        for (FeeKey feeKey : feeKeys) {
            // 创建查询条件，用于查询与当前费用键关联的费用值
            LambdaQueryWrapper<FeeValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FeeValue::getFeeKeyId, feeKey.getId());
            // 执行查询，获取费用值列表
            List<FeeValue> feeValues = feeValueService.list(queryWrapper);

            // 创建费用键值对象，用于封装当前费用键及其对应的费用值列表
            FeeKeyVo feeKeyVo = new FeeKeyVo();
            // 复制费用键属性到费用键值对象
            BeanUtils.copyProperties(feeKey, feeKeyVo);
            // 设置费用值列表到费用键值对象
            feeKeyVo.setFeeValueList(feeValues);

            // 将封装好的费用键值对象添加到费用信息列表中
            feeKeyVos.add(feeKeyVo);
        }

        // 返回包含所有费用信息的列表
        return feeKeyVos;
    }
}
