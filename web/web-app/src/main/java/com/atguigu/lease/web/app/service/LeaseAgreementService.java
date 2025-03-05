package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_agreement(租约信息表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface LeaseAgreementService extends IService<LeaseAgreement> {
    List<AgreementItemVo> listItem();

    AgreementDetailVo getDetailById(Long id);
}
