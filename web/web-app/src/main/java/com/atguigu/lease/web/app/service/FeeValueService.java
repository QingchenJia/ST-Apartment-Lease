package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【fee_value(杂项费用值表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface FeeValueService extends IService<FeeValue> {
    List<FeeValueVo> listByApartmentId(Long id);
}
