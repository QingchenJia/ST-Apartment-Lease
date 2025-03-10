package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author liubo
 * &#064;description  针对表【apartment_info(公寓信息表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface ApartmentInfoService extends IService<ApartmentInfo> {
    ApartmentDetailVo getDetailById(Long id);
}
