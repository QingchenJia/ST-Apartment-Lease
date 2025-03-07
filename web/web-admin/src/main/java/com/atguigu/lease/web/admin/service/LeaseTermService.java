package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.LeaseTerm;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_term(租期)】的数据库操作Service
 * &#064;createDate  2023-07-24 15:48:00
 */
public interface LeaseTermService extends IService<LeaseTerm> {
    List<LeaseTerm> listByRoomId(Long id);
}
