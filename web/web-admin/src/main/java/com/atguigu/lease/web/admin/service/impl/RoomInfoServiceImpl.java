package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.admin.mapper.RoomInfoMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【room_info(房间信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo> implements RoomInfoService {
    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private AttrValueService attrValueService;

    @Resource
    private RoomAttrValueService roomAttrValueService;

    @Resource
    private FacilityInfoService facilityInfoService;

    @Resource
    private RoomFacilityService roomFacilityService;

    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private RoomLabelService roomLabelService;

    @Resource
    private PaymentTypeService paymentTypeService;

    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Resource
    private LeaseTermService leaseTermService;

    @Resource
    private RoomLeaseTermService roomLeaseTermService;

    @Resource
    @Lazy
    private LeaseAgreementService leaseAgreementService;

    @Resource
    @Lazy
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存或更新房间信息
     *
     * @param roomSubmitVo 房间提交对象，包含房间的详细信息
     */
    @Override
    @Transactional
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        // 判断是否为更新操作
        boolean isUpdate = roomSubmitVo.getId() != null;

        // 执行房间信息的保存或更新
        saveOrUpdate(roomSubmitVo);
        Long roomId = roomSubmitVo.getId();

        // 如果是更新操作，先删除与房间相关的图形信息、属性值、设施、标签、支付方式和租赁条款
        if (isUpdate) {
            // 删除房间相关的图形信息
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper.eq(GraphInfo::getItemId, roomId)
                    .eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphInfoQueryWrapper);

            // 删除房间属性值
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
            roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, roomId);
            roomAttrValueService.remove(roomAttrValueQueryWrapper);

            // 删除房间设施
            LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
            roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, roomId);
            roomFacilityService.remove(roomFacilityQueryWrapper);

            // 删除房间标签
            LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
            roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomId);
            roomLabelService.remove(roomLabelQueryWrapper);

            // 删除房间支付方式
            LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
            roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, roomId);
            roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

            // 删除房间租赁条款
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomId);
            roomLeaseTermService.remove(roomLeaseTermQueryWrapper);

            // 房间数据更新后，删除缓存
            String key = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(key);
        }

        // 处理房间图形信息
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            List<GraphInfo> graphInfos = graphVoList.stream()
                    .map(graphVo -> {
                        GraphInfo graphInfo = new GraphInfo();
                        BeanUtils.copyProperties(graphVo, graphInfo);

                        graphInfo.setItemId(roomId);
                        graphInfo.setItemType(ItemType.ROOM);

                        return graphInfo;
                    })
                    .toList();
            graphInfoService.saveBatch(graphInfos);
        }

        // 处理房间属性值
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)) {
            List<RoomAttrValue> roomAttrValues = attrValueIds.stream()
                    .map(attrValueId -> {
                        RoomAttrValue roomAttrValue = new RoomAttrValue();
                        roomAttrValue.setRoomId(roomId);
                        roomAttrValue.setAttrValueId(attrValueId);

                        return roomAttrValue;
                    })
                    .toList();
            roomAttrValueService.saveBatch(roomAttrValues);
        }

        // 处理房间设施
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
            List<RoomFacility> roomFacilities = facilityInfoIds.stream()
                    .map(facilityInfoId -> {
                        RoomFacility roomFacility = new RoomFacility();
                        roomFacility.setRoomId(roomId);
                        roomFacility.setFacilityId(facilityInfoId);

                        return roomFacility;
                    })
                    .toList();
            roomFacilityService.saveBatch(roomFacilities);
        }

        // 处理房间标签
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIds)) {
            List<RoomLabel> roomLabels = labelInfoIds.stream()
                    .map(labelInfoId -> {
                        RoomLabel roomLabel = new RoomLabel();
                        roomLabel.setRoomId(roomId);
                        roomLabel.setLabelId(labelInfoId);

                        return roomLabel;
                    })
                    .toList();
            roomLabelService.saveBatch(roomLabels);
        }

        // 处理房间支付方式
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            List<RoomPaymentType> roomPaymentTypes = paymentTypeIds.stream()
                    .map(paymentTypeId -> {
                        RoomPaymentType roomPaymentType = new RoomPaymentType();
                        roomPaymentType.setRoomId(roomId);
                        roomPaymentType.setPaymentTypeId(paymentTypeId);

                        return roomPaymentType;
                    })
                    .toList();
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }

        // 处理房间租赁条款
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            List<RoomLeaseTerm> roomLeaseTerms = leaseTermIds.stream()
                    .map(leaseTermId -> {
                        RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                        roomLeaseTerm.setRoomId(roomId);
                        roomLeaseTerm.setLeaseTermId(leaseTermId);

                        return roomLeaseTerm;
                    })
                    .toList();
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }
    }

    /**
     * 根据分页和查询条件获取房间列表
     *
     * @param current 当前页码
     * @param size    每页记录数
     * @param queryVo 查询条件对象
     * @return 分页的房间项视图对象
     */
    @Override
    public IPage<RoomItemVo> pageItem(long current, long size, RoomQueryVo queryVo) {
        // 创建分页对象并设置当前页和每页大小
        Page<RoomInfo> page = new Page<>(current, size);
        // 获取查询条件中的公寓ID
        Long apartmentId = queryVo.getApartmentId();

        // 创建查询包装器并设置查询条件
        LambdaQueryWrapper<RoomInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(apartmentId != null, RoomInfo::getApartmentId, apartmentId)
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);
        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<RoomInfo> roomInfos = page.getRecords();
        // 将查询结果转换为房间项视图对象列表
        List<RoomItemVo> roomItemVos = roomInfos.stream()
                .map(roomInfo -> {
                    // 创建房间项视图对象并复制属性
                    RoomItemVo roomItemVo = new RoomItemVo();
                    BeanUtils.copyProperties(roomInfo, roomItemVo);

                    // 查询房间的租赁协议
                    LambdaQueryWrapper<LeaseAgreement> leaseAgreementQueryWrapper = new LambdaQueryWrapper<>();
                    leaseAgreementQueryWrapper.eq(LeaseAgreement::getRoomId, roomInfo.getId());
                    LeaseAgreement leaseAgreement = leaseAgreementService.getOne(leaseAgreementQueryWrapper);
                    if (leaseAgreement != null) {
                        // 设置租赁结束日期
                        roomItemVo.setLeaseEndDate(leaseAgreement.getLeaseEndDate());

                        // 判断是否入住
                        boolean isCheckIn = leaseAgreement.getStatus() == LeaseStatus.SIGNED || leaseAgreement.getStatus() == LeaseStatus.WITHDRAWING;
                        roomItemVo.setIsCheckIn(isCheckIn);
                    }

                    // 查询并设置公寓信息
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomItemVo.getApartmentId());
                    roomItemVo.setApartmentInfo(apartmentInfo);

                    return roomItemVo;
                })
                .toList();

        // 创建结果分页对象并复制属性
        Page<RoomItemVo> resultPage = new Page<>();
        BeanUtils.copyProperties(page, resultPage, "records");

        // 设置分页结果列表
        resultPage.setRecords(roomItemVos);

        return resultPage;
    }

    /**
     * 根据房间ID获取房间详情
     *
     * @param id 房间ID
     * @return 返回包含房间详细信息的RoomDetailVo对象
     */
    @Override
    public RoomDetailVo getDetailById(Long id) {
        // 初始化RoomDetailVo对象
        RoomDetailVo roomDetailVo = new RoomDetailVo();

        // 根据ID获取房间信息，并将属性复制到RoomDetailVo对象中
        RoomInfo roomInfo = getById(id);
        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        // 根据房间信息中的公寓ID获取公寓信息，并设置到RoomDetailVo对象中
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        roomDetailVo.setApartmentInfo(apartmentInfo);

        // 获取房间相关的图片信息，并设置到RoomDetailVo对象中
        List<GraphVo> graphVos = graphInfoService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(graphVos)) {
            roomDetailVo.setGraphVoList(graphVos);
        }

        // 获取房间相关的属性信息，并设置到RoomDetailVo对象中
        List<AttrValueVo> attrValueVos = attrValueService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(attrValueVos)) {
            roomDetailVo.setAttrValueVoList(attrValueVos);
        }

        // 获取房间相关的设施信息，并设置到RoomDetailVo对象中
        List<FacilityInfo> facilityInfos = facilityInfoService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(facilityInfos)) {
            roomDetailVo.setFacilityInfoList(facilityInfos);
        }

        // 获取房间相关的标签信息，并设置到RoomDetailVo对象中
        List<LabelInfo> labelInfos = labelInfoService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(labelInfos)) {
            roomDetailVo.setLabelInfoList(labelInfos);
        }

        // 获取房间相关的付款方式信息，并设置到RoomDetailVo对象中
        List<PaymentType> paymentTypes = paymentTypeService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(paymentTypes)) {
            roomDetailVo.setPaymentTypeList(paymentTypes);
        }

        // 获取房间相关的租赁期限信息，并设置到RoomDetailVo对象中
        List<LeaseTerm> leaseTerms = leaseTermService.listByRoomId(id);
        if (!CollectionUtils.isEmpty(leaseTerms)) {
            roomDetailVo.setLeaseTermList(leaseTerms);
        }

        // 返回包含房间详细信息的RoomDetailVo对象
        return roomDetailVo;
    }

    /**
     * 根据房间ID删除房间信息及其相关联的信息
     * 这包括从多个相关表中删除与该房间ID关联的所有记录
     *
     * @param id 房间的唯一标识符
     */
    @Override
    public void removeRoomById(Long id) {
        // 删除房间基本信息
        removeById(id);

        // 删除与房间关联的图表信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);
        graphInfoService.remove(graphInfoQueryWrapper);

        // 删除房间属性值信息
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(roomAttrValueQueryWrapper);

        // 删除房间设施信息
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(roomFacilityQueryWrapper);

        // 删除房间标签信息
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(roomLabelQueryWrapper);

        // 删除房间支付类型信息
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(roomPaymentTypeQueryWrapper);

        // 删除房间租赁条款信息
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(roomLeaseTermQueryWrapper);

        // 房间数据更新后，删除缓存
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        redisTemplate.delete(key);
    }
}
