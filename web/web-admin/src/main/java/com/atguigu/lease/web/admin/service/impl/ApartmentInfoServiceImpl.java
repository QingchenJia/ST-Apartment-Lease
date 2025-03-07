package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.admin.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo> implements ApartmentInfoService {
    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private FacilityInfoService facilityInfoService;

    @Resource
    private ApartmentFacilityService apartmentFacilityService;

    @Resource
    private FeeValueService feeValueService;

    @Resource
    private ApartmentFeeValueService apartmentFeeValueService;

    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private ApartmentLabelService apartmentLabelService;

    @Resource
    @Lazy
    private RoomInfoService roomInfoService;

    @Resource
    @Lazy
    private LeaseAgreementService leaseAgreementService;

    /**
     * 保存或更新公寓信息
     * <p>
     * 当公寓信息已存在时，会先删除与该公寓相关的所有设施、费用、标签等信息，然后根据新提交的数据重新保存这些信息
     * 这是为了确保数据库中的信息与提交的数据一致，避免数据不同步的问题
     *
     * @param apartmentSubmitVo 公寓提交对象，包含公寓的基本信息以及关联的设施、费用、标签等
     */
    @Override
    @Transactional
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        // 判断是否为更新操作
        boolean isUpdate = apartmentSubmitVo.getId() != null;

        // 保存或更新公寓基本信息
        saveOrUpdate(apartmentSubmitVo);
        Long apartmentId = apartmentSubmitVo.getId();

        // 如果是更新操作，先删除原有的关联信息
        if (isUpdate) {
            // 删除与公寓相关的图表信息
            LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoQueryWrapper.eq(GraphInfo::getItemId, apartmentId)
                    .eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoService.remove(graphInfoQueryWrapper);

            // 删除与公寓相关的设施信息
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentId);
            apartmentFacilityService.remove(apartmentFacilityQueryWrapper);

            // 删除与公寓相关的费用信息
            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentId);
            apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);

            // 删除与公寓相关的标签信息
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentId);
            apartmentLabelService.remove(apartmentLabelQueryWrapper);
        }

        // 处理公寓的图表信息
        List<GraphVo> graphVos = apartmentSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVos)) {
            List<GraphInfo> graphInfos = graphVos.stream()
                    .map(graphVo -> {
                        GraphInfo graphInfo = new GraphInfo();
                        BeanUtils.copyProperties(graphVo, graphInfo);
                        graphInfo.setItemId(apartmentId);
                        graphInfo.setItemType(ItemType.APARTMENT);
                        return graphInfo;
                    })
                    .toList();
            graphInfoService.saveBatch(graphInfos);
        }

        // 处理公寓的设施信息
        List<Long> facilityInfoIds = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
            List<ApartmentFacility> apartmentFacilities = facilityInfoIds.stream()
                    .map(facilityInfoId -> {
                        ApartmentFacility apartmentFacility = new ApartmentFacility();
                        apartmentFacility.setApartmentId(apartmentId);
                        apartmentFacility.setFacilityId(facilityInfoId);
                        return apartmentFacility;
                    })
                    .toList();
            apartmentFacilityService.saveBatch(apartmentFacilities);
        }

        // 处理公寓的费用信息
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            List<ApartmentFeeValue> apartmentFeeValues = feeValueIds.stream()
                    .map(feeValueId -> {
                        ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                        apartmentFeeValue.setApartmentId(apartmentId);
                        apartmentFeeValue.setFeeValueId(feeValueId);
                        return apartmentFeeValue;
                    })
                    .toList();
            apartmentFeeValueService.saveBatch(apartmentFeeValues);
        }

        // 处理公寓的标签信息
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if (!CollectionUtils.isEmpty(labelIds)) {
            List<ApartmentLabel> apartmentLabels = labelIds.stream()
                    .map(labelId -> {
                        ApartmentLabel apartmentLabel = new ApartmentLabel();
                        apartmentLabel.setApartmentId(apartmentId);
                        apartmentLabel.setLabelId(labelId);
                        return apartmentLabel;
                    })
                    .toList();
            apartmentLabelService.saveBatch(apartmentLabels);
        }
    }

    /**
     * 根据分页参数和查询条件获取公寓信息列表
     *
     * @param current 当前页码
     * @param size    每页记录数
     * @param queryVo 查询条件对象，包含省份、城市、地区ID等信息
     * @return 返回一个分页对象，包含公寓信息的列表
     */
    @Override
    public IPage<ApartmentItemVo> pageItem(long current, long size, ApartmentQueryVo queryVo) {
        // 创建分页对象，传入当前页码和每页记录数
        Page<ApartmentInfo> page = new Page<>(current, size);

        // 获取查询条件中的省份、城市、地区ID
        Long provinceId = queryVo.getProvinceId();
        Long cityId = queryVo.getCityId();
        Long districtId = queryVo.getDistrictId();

        // 创建查询包装器，用于拼接查询条件
        LambdaQueryWrapper<ApartmentInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 根据省份ID、城市ID、地区ID生成查询条件，如果ID不为空，则添加相应的查询条件
        queryWrapper.eq(provinceId != null, ApartmentInfo::getProvinceId, provinceId)
                .eq(cityId != null, ApartmentInfo::getCityId, cityId)
                .eq(districtId != null, ApartmentInfo::getDistrictId, districtId)
                .eq(ApartmentInfo::getIsRelease, ReleaseStatus.RELEASED);

        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果，即公寓信息列表
        List<ApartmentInfo> apartmentInfos = page.getRecords();

        // 将查询到的公寓信息转换为公寓项VO列表
        List<ApartmentItemVo> apartmentItemVos = apartmentInfos.stream()
                .map(apartmentInfo -> {
                    // 创建公寓项VO对象，并将公寓信息的属性复制到VO对象中
                    ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
                    BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

                    // 创建查询包装器，用于查询房间信息
                    LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
                    // 根据公寓ID查询房间总数
                    roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, apartmentInfo.getId())
                            .eq(RoomInfo::getIsRelease, ReleaseStatus.RELEASED);
                    long totalCount = roomInfoService.count(roomInfoQueryWrapper);

                    // 创建查询包装器，用于查询租赁协议信息
                    LambdaQueryWrapper<LeaseAgreement> leaseAgreementQueryWrapper = new LambdaQueryWrapper<>();
                    // 根据公寓ID和租赁协议状态查询已签约和撤约中的房间数量
                    leaseAgreementQueryWrapper.eq(LeaseAgreement::getApartmentId, apartmentInfo.getId())
                            .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
                    long freeCount = totalCount - leaseAgreementService.count(leaseAgreementQueryWrapper);

                    // 设置公寓项VO的房间总数和空闲房间数量
                    apartmentItemVo.setTotalRoomCount(totalCount);
                    apartmentItemVo.setFreeRoomCount(freeCount);

                    // 返回公寓项VO对象
                    return apartmentItemVo;
                })
                .toList();

        // 创建结果分页对象，用于返回公寓项VO列表
        Page<ApartmentItemVo> resultPage = new Page<>();
        // 将原分页对象的属性复制到结果分页对象中，但不包括记录列表
        BeanUtils.copyProperties(page, resultPage, "records");
        // 设置结果分页对象的记录列表为转换后的公寓项VO列表
        resultPage.setRecords(apartmentItemVos);

        // 返回结果分页对象
        return resultPage;
    }

    /**
     * 根据ID获取公寓详细信息
     * <p>
     * 此方法通过聚合来自不同表的数据（如公寓信息、图片、设施、费用等），构建并返回一个公寓详情对象
     * 它首先根据给定的ID获取基本公寓信息，然后查询相关图片，设施，费用和标签信息，并将这些信息设置到公寓详情对象中
     *
     * @param id 公寓信息的ID
     * @return 包含公寓详细信息的ApartmentDetailVo对象
     */
    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        // 创建一个公寓详情对象，用于存储从数据库查询到的公寓信息
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();

        // 根据给定的ID获取公寓基本信息，并将其属性复制到公寓详情对象中
        ApartmentInfo apartmentInfo = getById(id);
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);

        // 查询并设置公寓图片信息
        List<GraphVo> graphVos = graphInfoService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(graphVos)) {
            apartmentDetailVo.setGraphVoList(graphVos);
        }

        // 查询并设置公寓设施信息
        List<FacilityInfo> facilityInfos = facilityInfoService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(facilityInfos)) {
            apartmentDetailVo.setFacilityInfoList(facilityInfos);
        }

        // 查询并设置公寓费用信息
        List<FeeValueVo> feeValueVos = feeValueService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(facilityInfos)) {
            apartmentDetailVo.setFeeValueVoList(feeValueVos);
        }

        // 查询并设置公寓标签信息
        List<LabelInfo> labelInfos = labelInfoService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(labelInfos)) {
            apartmentDetailVo.setLabelInfoList(labelInfos);
        }

        // 返回填充了所有相关信息的公寓详情对象
        return apartmentDetailVo;
    }

    /**
     * 根据ID删除公寓信息
     * 此方法首先检查是否有关联的房间信息，如果有，则抛出删除错误异常
     * 否则，依次删除公寓信息、公寓相关的图形信息、公寓设施、公寓费用值和公寓标签
     *
     * @param id 公寓ID
     * @throws LeaseException 如果尝试删除有关联房间的公寓时抛出
     */
    @Override
    @Transactional
    public void removeApartmentById(Long id) {
        // 创建查询条件，检查是否存在房间关联
        LambdaQueryWrapper<RoomInfo> roomInfoQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoQueryWrapper.eq(RoomInfo::getApartmentId, id);

        // 检查是否有房间关联
        long count = roomInfoService.count(roomInfoQueryWrapper);
        if (count > 0) {
            // 如果存在房间关联，抛出删除错误异常
            throw new LeaseException(ResultCodeEnum.DELETE_ERROR);
        }

        // 删除公寓信息
        removeById(id);

        // 创建查询条件，删除公寓相关的图形信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoService.remove(graphInfoQueryWrapper);

        // 创建查询条件，删除公寓设施信息
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(apartmentFacilityQueryWrapper);

        // 创建查询条件，删除公寓费用值信息
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(apartmentFeeValueQueryWrapper);

        // 创建查询条件，删除公寓标签信息
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(apartmentLabelQueryWrapper);
    }
}
