package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.web.admin.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement> implements LeaseAgreementService {
    @Resource
    @Lazy
    private RoomInfoService roomInfoService;

    @Resource
    @Lazy
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private PaymentTypeService paymentTypeService;

    @Resource
    private LeaseTermService leaseTermService;

    /**
     * 根据查询条件分页获取协议列表
     *
     * @param current 当前页码
     * @param size 每页记录数
     * @param queryVo 查询条件对象，包含公寓ID、姓名和电话等查询参数
     * @return 返回包含协议列表的分页对象
     */
    @Override
    public IPage<AgreementVo> pageItem(long current, long size, AgreementQueryVo queryVo) {
        // 初始化分页对象
        Page<LeaseAgreement> page = new Page<>(current, size);

        // 获取查询条件
        Long apartmentId = queryVo.getApartmentId();
        String name = queryVo.getName();
        String phone = queryVo.getPhone();

        // 构建查询封装器，根据查询条件进行等值查询
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(apartmentId != null, LeaseAgreement::getApartmentId, apartmentId)
                .eq(StringUtils.hasText(name), LeaseAgreement::getName, name)
                .eq(StringUtils.hasText(phone), LeaseAgreement::getPhone, phone);
        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<LeaseAgreement> leaseAgreements = page.getRecords();
        // 将查询结果转换为AgreementVo对象列表
        List<AgreementVo> agreementVos = leaseAgreements.stream()
                .map(leaseAgreement -> {
                    AgreementVo agreementVo = new AgreementVo();
                    BeanUtils.copyProperties(leaseAgreement, agreementVo);

                    // 根据公寓ID获取公寓信息并设置到Vo对象中
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
                    agreementVo.setApartmentInfo(apartmentInfo);

                    // 根据房间ID获取房间信息并设置到Vo对象中
                    RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId());
                    agreementVo.setRoomInfo(roomInfo);

                    // 根据支付类型ID获取支付类型信息并设置到Vo对象中
                    PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());
                    agreementVo.setPaymentType(paymentType);

                    // 根据租赁期限ID获取租赁期限信息并设置到Vo对象中
                    LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());
                    agreementVo.setLeaseTerm(leaseTerm);

                    return agreementVo;
                })
                .toList();

        // 初始化结果分页对象
        Page<AgreementVo> resultPage = new Page<>();
        // 复制分页属性
        BeanUtils.copyProperties(page, resultPage, "records");
        // 设置转换后的记录列表
        resultPage.setRecords(agreementVos);

        // 返回结果分页对象
        return resultPage;
    }

    /**
     * 根据ID获取协议详细信息
     * 此方法通过聚合多个相关实体的信息来构建一个协议视图对象
     * 它从数据库中检索协议的基本信息，并关联公寓、房间、支付方式和租赁条款的信息
     *
     * @param id 协议的唯一标识符
     * @return 返回一个填充了协议详细信息的AgreementVo对象
     */
    @Override
    public AgreementVo getAgreementById(Long id) {
        // 初始化协议视图对象
        AgreementVo agreementVo = new AgreementVo();

        // 根据ID获取协议基本信息
        LeaseAgreement leaseAgreement = getById(id);
        // 将协议基本信息复制到视图对象中
        BeanUtils.copyProperties(leaseAgreement, agreementVo);

        // 根据协议中的公寓ID获取公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
        // 将公寓信息设置到视图对象中
        agreementVo.setApartmentInfo(apartmentInfo);

        // 根据协议中的房间ID获取房间信息
        RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId());
        // 将房间信息设置到视图对象中
        agreementVo.setRoomInfo(roomInfo);

        // 根据协议中的支付方式ID获取支付方式信息
        PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());
        // 将支付方式信息设置到视图对象中
        agreementVo.setPaymentType(paymentType);

        // 根据协议中的租赁条款ID获取租赁条款信息
        LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());
        // 将租赁条款信息设置到视图对象中
        agreementVo.setLeaseTerm(leaseTerm);

        // 返回填充完毕的协议视图对象
        return agreementVo;
    }
}
