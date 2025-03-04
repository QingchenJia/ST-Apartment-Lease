package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
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
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private RoomPaymentTypeService roomPaymentTypeService;

    @Resource
    private PaymentTypeService paymentTypeService;

    @Resource
    private LeaseAgreementService leaseAgreementService;

    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private RoomLabelService roomLabelService;

    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private ApartmentLabelService apartmentLabelService;

    @Resource
    private RoomAttrValueService roomAttrValueService;

    @Resource
    private AttrValueService attrValueService;

    @Resource
    private AttrKeyService attrKeyService;

    @Resource
    private RoomFacilityService roomFacilityService;

    @Resource
    private FacilityInfoService facilityInfoService;

    @Resource
    private ApartmentFeeValueService apartmentFeeValueService;

    @Resource
    private FeeValueService feeValueService;

    @Resource
    private FeeKeyService feeKeyService;

    @Resource
    private RoomLeaseTermService roomLeaseTermService;

    @Resource
    private LeaseTermService leaseTermService;

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
                    LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
                    graphInfoQueryWrapper.eq(GraphInfo::getItemId, roomInfo.getId())
                            .eq(GraphInfo::getItemType, ItemType.ROOM);

                    List<GraphInfo> graphInfos = graphInfoService.list(graphInfoQueryWrapper);
                    List<GraphVo> graphVos = graphInfos.stream()
                            .map(graphInfo -> {
                                GraphVo graphVo = new GraphVo();
                                BeanUtils.copyProperties(graphInfo, graphVo);
                                return graphVo;
                            })
                            .toList();

                    roomItemVo.setGraphVoList(graphVos);

                    // 查询房间相关的标签信息
                    LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
                    roomLabelQueryWrapper.eq(RoomLabel::getRoomId, roomInfo.getId());

                    List<RoomLabel> roomLabels = roomLabelService.list(roomLabelQueryWrapper);
                    List<LabelInfo> labelInfos = roomLabels.stream()
                            .map(roomLabel -> labelInfoService.getById(roomLabel.getLabelId()))
                            .toList();

                    roomItemVo.setLabelInfoList(labelInfos);

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
        // 初始化RoomDetailVo对象
        RoomDetailVo roomDetailVo = new RoomDetailVo();

        // 根据ID获取房间信息，并将属性复制到RoomDetailVo对象中
        RoomInfo roomInfo = getById(id);
        BeanUtils.copyProperties(roomInfo, roomDetailVo);

        // 初始化ApartmentItemVo对象
        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();

        // 根据房间信息中的公寓ID获取公寓信息，并将属性复制到ApartmentItemVo对象中
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        // 查询与公寓关联的标签信息
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentInfo.getId());

        List<ApartmentLabel> apartmentLabels = apartmentLabelService.list(apartmentLabelQueryWrapper);
        List<LabelInfo> labelInfosOfApartment = apartmentLabels.stream()
                .map(apartmentLabel -> labelInfoService.getById(apartmentLabel.getLabelId()))
                .toList();

        // 将公寓的标签信息设置到ApartmentItemVo对象中
        apartmentItemVo.setLabelInfoList(labelInfosOfApartment);

        // 查询与公寓关联的图表信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapperOfApartment = new LambdaQueryWrapper<>();
        graphInfoQueryWrapperOfApartment.eq(GraphInfo::getItemId, apartmentInfo.getId())
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);

        List<GraphInfo> graphInfosOfApartment = graphInfoService.list(graphInfoQueryWrapperOfApartment);
        List<GraphVo> graphVosOfApartment = graphInfosOfApartment.stream()
                .map(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    return graphVo;
                })
                .toList();

        // 将公寓的图表信息设置到ApartmentItemVo对象中
        apartmentItemVo.setGraphVoList(graphVosOfApartment);

        // 查询公寓下已发布的房间信息，并找出租金最低的房间
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, roomInfo.getApartmentId())
                .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);

        List<RoomInfo> roomInfos = list(roomInfoQueryWrapper);
        RoomInfo roomInfoWithMinRent = roomInfos.stream()
                .min(Comparator.comparing(RoomInfo::getRent))
                .get();

        // 将最低租金信息设置到ApartmentItemVo对象中
        apartmentItemVo.setMinRent(roomInfoWithMinRent.getRent());

        // 将ApartmentItemVo对象设置到RoomDetailVo对象中
        roomDetailVo.setApartmentItemVo(apartmentItemVo);

        // 查询与房间关联的图表信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapperOfRoom = new LambdaQueryWrapper<>();
        graphInfoQueryWrapperOfRoom.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);

        List<GraphInfo> graphInfosOfRoom = graphInfoService.list(graphInfoQueryWrapperOfRoom);
        List<GraphVo> graphVosOfRoom = graphInfosOfRoom.stream()
                .map(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    return graphVo;
                })
                .toList();

        // 将房间的图表信息设置到RoomDetailVo对象中
        roomDetailVo.setGraphVoList(graphVosOfRoom);

        // 查询房间的属性值信息
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);

        List<RoomAttrValue> roomAttrValues = roomAttrValueService.list(roomAttrValueQueryWrapper);
        List<AttrValueVo> attrValueVos = roomAttrValues.stream()
                .map(roomAttrValue -> {
                    AttrValueVo attrValueVo = new AttrValueVo();

                    AttrValue attrValue = attrValueService.getById(roomAttrValue.getAttrValueId());
                    BeanUtils.copyProperties(attrValue, attrValueVo);

                    AttrKey attrKey = attrKeyService.getById(attrValue.getAttrKeyId());
                    attrValueVo.setAttrKeyName(attrKey.getName());

                    return attrValueVo;
                })
                .toList();

        // 将房间的属性值信息设置到RoomDetailVo对象中
        roomDetailVo.setAttrValueVoList(attrValueVos);

        // 查询房间的设施信息
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);

        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);
        List<FacilityInfo> facilityInfos = roomFacilities.stream()
                .map(roomFacility -> facilityInfoService.getById(roomFacility.getFacilityId()))
                .toList();

        // 将房间的设施信息设置到RoomDetailVo对象中
        roomDetailVo.setFacilityInfoList(facilityInfos);

        // 查询房间的标签信息
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);

        List<RoomLabel> roomLabels = roomLabelService.list(roomLabelQueryWrapper);
        List<LabelInfo> labelInfosOfRoom = roomLabels.stream()
                .map(roomLabel -> labelInfoService.getById(roomLabel.getLabelId()))
                .toList();

        // 将房间的标签信息设置到RoomDetailVo对象中
        roomDetailVo.setLabelInfoList(labelInfosOfRoom);

        // 查询房间的支付方式信息
        LambdaQueryWrapper<RoomPaymentType> roomPaymentTypeQueryWrapper = new LambdaQueryWrapper<>();
        roomPaymentTypeQueryWrapper.eq(RoomPaymentType::getRoomId, id);

        List<RoomPaymentType> roomPaymentTypes = roomPaymentTypeService.list(roomPaymentTypeQueryWrapper);
        List<PaymentType> paymentTypes = roomPaymentTypes.stream()
                .map(roomPaymentType -> paymentTypeService.getById(roomPaymentType.getPaymentTypeId()))
                .toList();

        // 将房间的支付方式信息设置到RoomDetailVo对象中
        roomDetailVo.setPaymentTypeList(paymentTypes);

        // 查询公寓的费用值信息
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, roomInfo.getApartmentId());

        List<ApartmentFeeValue> apartmentFeeValues = apartmentFeeValueService.list(apartmentFeeValueQueryWrapper);
        List<FeeValueVo> feeValueVos = apartmentFeeValues.stream()
                .map(apartmentFeeValue -> {
                    FeeValueVo feeValueVo = new FeeValueVo();

                    FeeValue feeValue = feeValueService.getById(apartmentFeeValue.getFeeValueId());
                    BeanUtils.copyProperties(feeValue, feeValueVo);

                    FeeKey feeKey = feeKeyService.getById(feeValue.getFeeKeyId());
                    feeValueVo.setFeeKeyName(feeKey.getName());

                    return feeValueVo;
                })
                .toList();

        // 将公寓的费用值信息设置到RoomDetailVo对象中
        roomDetailVo.setFeeValueVoList(feeValueVos);

        // 查询房间的租赁条款信息
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);

        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermService.list(roomLeaseTermQueryWrapper);
        List<LeaseTerm> leaseTerms = roomLeaseTerms.stream()
                .map(roomLeaseTerm -> leaseTermService.getById(roomLeaseTerm.getLeaseTermId()))
                .toList();

        // 将房间的租赁条款信息设置到RoomDetailVo对象中
        roomDetailVo.setLeaseTermList(leaseTerms);

        // 返回包含房间详细信息的RoomDetailVo对象
        return roomDetailVo;
    }
}
