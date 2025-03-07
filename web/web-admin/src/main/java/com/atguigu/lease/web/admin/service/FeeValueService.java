package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【fee_value(杂项费用值表)】的数据库操作Service
 * &#064;createDate  2023-07-24 15:48:00
 */
public interface FeeValueService extends IService<FeeValue> {
    List<FeeValueVo> listByApartmentId(Long id);
}
