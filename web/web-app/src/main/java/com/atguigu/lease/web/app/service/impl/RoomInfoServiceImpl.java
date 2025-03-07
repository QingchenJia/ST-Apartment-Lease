package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【room_info(房间信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo> implements RoomInfoService {
    @Resource
    @Lazy
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Resource
    private PaymentTypeService paymentTypeService;

    @Resource
    @Lazy
    private LeaseAgreementService leaseAgreementService;

    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private AttrValueService attrValueService;

    @Resource
    private FacilityInfoService facilityInfoService;

    @Resource
    private FeeValueService feeValueService;

    @Resource
    private LeaseTermService leaseTermService;

    @Resource
    @Lazy
    private BrowsingHistoryService browsingHistoryService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据分页和查询条件获取房间列表
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param queryVo 查询条件对象
     * @return 分页的房间项视图对象
     */
    @Override
    public IPage<RoomItemVo> pageItem(long current, long size, RoomQueryVo queryVo) {
        // 初始化分页对象
        Page<RoomInfo> page = new Page<>(current, size);

        // 获取查询条件
        Long districtId = queryVo.getDistrictId();
        BigDecimal minRent = queryVo.getMinRent();
        BigDecimal maxRent = queryVo.getMaxRent();
        Long paymentTypeId = queryVo.getPaymentTypeId();
        String orderType = queryVo.getOrderType();

        // 创建公寓信息查询条件
        LambdaQueryWrapper<ApartmentInfo> apartmentInfoQueryWrapper = new LambdaQueryWrapper<>();
        apartmentInfoQueryWrapper.eq(districtId != null, ApartmentInfo::getDistrictId, districtId);

        // 查询符合条件的公寓信息
        List<ApartmentInfo> apartmentInfos = apartmentInfoService.list(apartmentInfoQueryWrapper);
        if (CollectionUtils.isEmpty(apartmentInfos)) {
            return null;
        }

        // 提取公寓信息ID列表
        List<Long> apartmentInfoIds = apartmentInfos.stream()
                .map(BaseEntity::getId)
                .toList();

        // 创建房间支付类型查询条件
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(paymentTypeId != null, RoomPaymentType::getPaymentTypeId, paymentTypeId);

        // 查询符合条件的房间支付类型
        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(roomPaymentTypeQueryWrapper);
        if (CollectionUtils.isEmpty(roomPaymentTypes)) {
            return null;
        }

        // 提取房间信息ID列表（根据支付类型）
        List<Long> roomInfoIdsWithPaymentType = roomPaymentTypes.stream()
                .map(RoomPaymentType::getRoomId)
                .toList();

        // 创建租赁协议查询条件
        LambdaQueryWrapper<LeaseAgreement> leaseAgreementQueryWrapper = new LambdaQueryWrapper<>();
        leaseAgreementQueryWrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
        List<LeaseAgreement> leaseAgreements = leaseAgreementService.list(leaseAgreementQueryWrapper);

        // 根据租赁协议确定不可用的房间信息ID列表
        List<Long> roomInfoIdsUnavailable;
        if (CollectionUtils.isEmpty(leaseAgreements)) {
            roomInfoIdsUnavailable = null;
        } else {
            roomInfoIdsUnavailable = leaseAgreements.stream()
                    .map(LeaseAgreement::getRoomId)
                    .toList();
        }

        // 创建房间信息查询条件
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.in(RoomInfo::getApartmentId, apartmentInfoIds)
                .ge(minRent != null, RoomInfo::getRent, minRent)
                .le(maxRent != null, RoomInfo::getRent, maxRent)
                .in(RoomInfo::getId, roomInfoIdsWithPaymentType)
                .notIn(roomInfoIdsUnavailable != null, RoomInfo::getId, roomInfoIdsUnavailable)
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED)
                .orderByAsc("asc".equals(orderType), RoomInfo::getRent)
                .orderByDesc("desc".equals(orderType), RoomInfo::getRent);

        // 执行房间信息分页查询
        page(page, roomInfoQueryWrapper);
        List<RoomInfo> roomInfos = page.getRecords();

        // 将房间信息转换为房间项视图对象列表
        List<RoomItemVo> roomItemVos = roomInfos.stream()
                .map(roomInfo -> {
                    RoomItemVo roomItemVo = new RoomItemVo();
                    BeanUtils.copyProperties(roomInfo, roomItemVo);

                    // 查询房间相关的图片信息
                    List<GraphVo> graphVos = graphInfoService.listByRoomId(roomInfo.getId());
                    if (!CollectionUtils.isEmpty(graphVos)) {
                        roomItemVo.setGraphVoList(graphVos);
                    }

                    // 查询房间相关的标签信息
                    List<LabelInfo> labelInfos = labelInfoService.listByRoomId(roomInfo.getId());
                    if (!CollectionUtils.isEmpty(labelInfos)) {
                        roomItemVo.setLabelInfoList(labelInfos);
                    }

                    // 查询房间相关的公寓信息
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());

                    roomItemVo.setApartmentInfo(apartmentInfo);

                    return roomItemVo;
                })
                .toList();

        // 创建结果分页对象并设置属性
        Page<RoomItemVo> resultPage = new Page<>();
        BeanUtils.copyProperties(page, resultPage, "records");

        resultPage.setRecords(roomItemVos);

        return resultPage;
    }

    /**
     * 根据房间ID获取房间详细信息
     *
     * @param id 房间ID
     * @return 包含房间详细信息的RoomDetailVo对象
     */
    @Override
    public RoomDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue()
                .get(key);

        // 缓存未命中房间详细信息
        if (roomDetailVo == null) {
            // 初始化RoomDetailVo对象
            roomDetailVo = new RoomDetailVo();

            // 根据ID获取房间信息，并将属性复制到RoomDetailVo对象中
            RoomInfo roomInfo = getById(id);
            BeanUtils.copyProperties(roomInfo, roomDetailVo);

            // 初始化ApartmentItemVo对象
            ApartmentItemVo apartmentItemVo = new ApartmentItemVo();

            // 根据房间信息中的公寓ID获取公寓信息，并将属性复制到ApartmentItemVo对象中
            ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
            BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

            // 获取公寓的标签信息，并设置到ApartmentItemVo对象中
            List<LabelInfo> labelInfosOfApartment = labelInfoService.listByApartmentId(apartmentInfo.getId());
            if (!CollectionUtils.isEmpty(labelInfosOfApartment)) {
                apartmentItemVo.setLabelInfoList(labelInfosOfApartment);
            }

            // 获取公寓的图表信息，并设置到ApartmentItemVo对象中
            List<GraphVo> graphVosOfApartment = graphInfoService.listByApartmentId(apartmentInfo.getId());
            if (!CollectionUtils.isEmpty(graphVosOfApartment)) {
                apartmentItemVo.setGraphVoList(graphVosOfApartment);
            }

            // 获取公寓下所有房间信息，以确定最小租金，并设置到ApartmentItemVo对象中
            List<RoomInfo> roomInfos = listByApartmentId(roomInfo.getApartmentId());
            if (!CollectionUtils.isEmpty(roomInfos)) {
                RoomInfo roomInfoWithMinRent = roomInfos.stream()
                        .min(Comparator.comparing(RoomInfo::getRent))
                        .get();

                apartmentItemVo.setMinRent(roomInfoWithMinRent.getRent());
            }

            // 将ApartmentItemVo对象设置到RoomDetailVo对象中
            roomDetailVo.setApartmentItemVo(apartmentItemVo);

            // 获取房间的图表信息，并设置到RoomDetailVo对象中
            List<GraphVo> graphVosOfRoom = graphInfoService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(graphVosOfRoom)) {
                roomDetailVo.setGraphVoList(graphVosOfRoom);
            }

            // 获取房间的属性信息，并设置到RoomDetailVo对象中
            List<AttrValueVo> attrValueVos = attrValueService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(attrValueVos)) {
                roomDetailVo.setAttrValueVoList(attrValueVos);
            }

            // 查询房间设施信息，并设置到RoomDetailVo对象中
            LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);

            List<FacilityInfo> facilityInfos = facilityInfoService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(facilityInfos)) {
                roomDetailVo.setFacilityInfoList(facilityInfos);
            }

            // 获取房间的标签信息，并设置到RoomDetailVo对象中
            List<LabelInfo> labelInfosOfRoom = labelInfoService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(labelInfosOfRoom)) {
                roomDetailVo.setLabelInfoList(labelInfosOfRoom);
            }

            // 获取房间的支付方式信息，并设置到RoomDetailVo对象中
            List<PaymentType> paymentTypes = paymentTypeService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(paymentTypes)) {
                roomDetailVo.setPaymentTypeList(paymentTypes);
            }

            // 获取公寓的费用信息，并设置到RoomDetailVo对象中
            List<FeeValueVo> feeValueVos = feeValueService.listByApartmentId(roomInfo.getApartmentId());
            if (!CollectionUtils.isEmpty(feeValueVos)) {
                roomDetailVo.setFeeValueVoList(feeValueVos);
            }

            // 获取房间的租赁条款信息，并设置到RoomDetailVo对象中
            List<LeaseTerm> leaseTerms = leaseTermService.listByRoomId(id);
            if (!CollectionUtils.isEmpty(leaseTerms)) {
                roomDetailVo.setLeaseTermList(leaseTerms);
            }

            // 缓存房间详细信息
            redisTemplate.opsForValue()
                    .set(key, roomDetailVo);
        }

        // 保存此次查询的浏览记录
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        browsingHistoryService.recordHistory(id, loginUser.getId());

        // 返回包含房间详细信息的RoomDetailVo对象
        return roomDetailVo;
    }

    /**
     * 根据公寓ID列出所有发布的房间信息
     *
     * @param id 公寓ID，用于查询关联的房间信息
     * @return 返回一个包含所有关联房间信息的列表
     */
    @Override
    public List<RoomInfo> listByApartmentId(Long id) {
        // 创建一个Lambda查询包装器，用于构建查询条件
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();

        // 设置查询条件：公寓ID等于传入的ID且房间发布状态为已发布
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, id)
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);

        // 执行查询并返回结果列表
        return list(roomInfoQueryWrapper);
    }

    /**
     * 根据公寓ID分页查询房间项
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param id      公寓ID
     * @return 分页的房间项VO列表
     */
    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(long current, long size, Long id) {
        // 创建分页对象
        Page<RoomInfo> page = new Page<>(current, size);

        // 创建查询条件
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomInfo::getApartmentId, id)
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);

        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<RoomInfo> roomInfos = page.getRecords();

        // 将RoomInfo列表转换为RoomItemVo列表
        List<RoomItemVo> roomItemVos = roomInfos.stream()
                .map(roomInfo -> {
                    RoomItemVo roomItemVo = new RoomItemVo();
                    BeanUtils.copyProperties(roomInfo, roomItemVo);

                    // 查询房间相关的图片信息
                    List<GraphVo> graphVos = graphInfoService.listByRoomId(roomInfo.getId());
                    if (!CollectionUtils.isEmpty(graphVos)) {
                        roomItemVo.setGraphVoList(graphVos);
                    }

                    // 查询房间相关的标签信息
                    List<LabelInfo> labelInfos = labelInfoService.listByRoomId(roomInfo.getId());
                    if (!CollectionUtils.isEmpty(labelInfos)) {
                        roomItemVo.setLabelInfoList(labelInfos);
                    }

                    // 查询房间相关的公寓信息
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());

                    roomItemVo.setApartmentInfo(apartmentInfo);

                    return roomItemVo;
                })
                .toList();

        // 创建结果分页对象并设置属性
        Page<RoomItemVo> resultPage = new Page<>();
        BeanUtils.copyProperties(page, resultPage, "records");

        resultPage.setRecords(roomItemVos);

        return resultPage;
    }
}
