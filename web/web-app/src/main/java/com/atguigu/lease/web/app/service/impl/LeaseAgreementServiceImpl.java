package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.web.app.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement> implements LeaseAgreementService {
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ApartmentInfoService apartmentInfoService;

    @Resource
    @Lazy
    private RoomInfoService roomInfoService;

    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private PaymentTypeService paymentTypeService;

    @Resource
    private LeaseTermService leaseTermService;

    /**
     * 获取当前登录用户的协议项列表
     * <p>
     * 此方法首先获取当前登录用户的信息，然后根据用户信息查询相关的租赁协议
     * 如果存在协议，则将每个协议项转换为视图对象（Vo），并附加相关的图形信息和公寓名称
     *
     * @return 协议项视图对象列表，如果用户没有相关的协议，则返回null
     */
    @Override
    public List<AgreementItemVo> listItem() {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        UserInfo userInfo = userInfoService.getById(loginUser.getId());

        // 构建查询条件，根据用户手机号查询租赁协议
        LambdaQueryWrapper<LeaseAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LeaseAgreement::getPhone, userInfo.getPhone());
        List<LeaseAgreement> leaseAgreements = list(queryWrapper);

        // 如果查询结果为空，直接返回null
        if (CollectionUtils.isEmpty(leaseAgreements)) {
            return null;
        }

        // 将查询到的租赁协议转换为协议项视图对象列表
        return leaseAgreements.stream()
                .map(leaseAgreement -> {
                    // 创建协议项视图对象并复制属性
                    AgreementItemVo agreementItemVo = new AgreementItemVo();
                    BeanUtils.copyProperties(leaseAgreement, agreementItemVo);

                    // 根据房间ID查询相关图形信息并设置到视图对象
                    List<GraphVo> graphVos = graphInfoService.listByRoomId(leaseAgreement.getRoomId());
                    agreementItemVo.setRoomGraphVoList(graphVos);

                    // 根据公寓ID查询公寓信息并设置公寓名称到视图对象
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
                    agreementItemVo.setApartmentName(apartmentInfo.getName());

                    // 返回转换后的视图对象
                    return agreementItemVo;
                })
                .toList();
    }

    /**
     * 根据ID获取租赁协议的详细信息
     * 此方法通过聚合来自不同实体的信息来构建一个详细的租赁协议视图对象
     * 它从数据库中检索相关信息，并将这些信息组合成一个易于使用的对象
     *
     * @param id 租赁协议的ID，用于查询特定的租赁协议详情
     * @return AgreementDetailVo 包含租赁协议详细信息的视图对象
     */
    @Override
    public AgreementDetailVo getDetailById(Long id) {
        // 初始化租赁协议详细视图对象
        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();

        // 通过ID获取租赁协议实体，并将其实例属性复制到视图对象中
        LeaseAgreement leaseAgreement = getById(id);
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);

        // 根据租赁协议中的公寓ID获取公寓信息，并设置公寓名称
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
        agreementDetailVo.setApartmentName(apartmentInfo.getName());

        // 获取与公寓相关的图表信息列表，并设置到视图对象中
        List<GraphVo> graphVosOfApartment = graphInfoService.listByApartmentId(leaseAgreement.getApartmentId());
        agreementDetailVo.setApartmentGraphVoList(graphVosOfApartment);

        // 根据租赁协议中的房间ID获取房间信息，并设置房间号
        RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId());
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());

        // 获取与房间相关的图表信息列表，并设置到视图对象中
        List<GraphVo> graphVosOfRoom = graphInfoService.listByRoomId(leaseAgreement.getRoomId());
        agreementDetailVo.setRoomGraphVoList(graphVosOfRoom);

        // 根据租赁协议中的支付类型ID获取支付类型信息，并设置支付类型名称
        PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());
        agreementDetailVo.setPaymentTypeName(paymentType.getName());

        // 根据租赁协议中的租赁期限ID获取租赁期限信息，并设置租赁期限的月数和单位
        LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        // 返回填充了详细信息的视图对象
        return agreementDetailVo;
    }
}
