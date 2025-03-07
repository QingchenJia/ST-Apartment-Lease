package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.ApartmentFacility;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.RoomFacility;
import com.atguigu.lease.web.admin.mapper.FacilityInfoMapper;
import com.atguigu.lease.web.admin.service.ApartmentFacilityService;
import com.atguigu.lease.web.admin.service.FacilityInfoService;
import com.atguigu.lease.web.admin.service.RoomFacilityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【facility_info(配套信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo> implements FacilityInfoService {
    @Resource
    private ApartmentFacilityService apartmentFacilityService;

    @Resource
    private RoomFacilityService roomFacilityService;

    /**
     * 根据公寓ID列出设施信息
     * <p>
     * 此方法通过公寓ID查询关联的设施信息，并进一步获取具体设施的详情
     * 它首先查询公寓设施关联表以找到所有与指定公寓ID相关的设施记录，
     * 然后提取这些记录中的设施ID，并使用这些ID来查询设施详情
     *
     * @param id 公寓ID，用于查询与该公寓相关的设施信息
     * @return 返回一个包含该公寓所有设施信息的列表，如果没有找到任何设施信息，则返回null
     */
    @Override
    public List<FacilityInfo> listByApartmentId(Long id) {
        // 查询公寓的设施信息
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        List<ApartmentFacility> apartmentFacilities = apartmentFacilityService.list(apartmentFacilityQueryWrapper);

        // 根据公寓设施信息获取具体的设施详情，并设置到公寓详情对象中
        if (CollectionUtils.isEmpty(apartmentFacilities)) {
            return null;
        }

        List<Long> facilityInfoIds = apartmentFacilities.stream()
                .map(ApartmentFacility::getFacilityId)
                .toList();

        return listByIds(facilityInfoIds);
    }

    /**
     * 根据房间ID列出设施信息
     * <p>
     * 此方法通过房间ID查询相关的房间设施，并返回这些设施的详细信息列表
     * 它首先根据房间ID查询房间设施表，然后根据查询结果获取设施ID列表，
     * 最后根据设施ID列表查询并返回设施信息列表
     *
     * @param id 房间ID，用于查询与该房间相关的设施信息
     * @return 返回一个FacilityInfo对象列表，包含与指定房间相关的所有设施信息，
     * 如果没有找到相关设施信息，则返回null
     */
    @Override
    public List<FacilityInfo> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID相关的房间设施列表
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);

        // 根据查询条件获取房间设施列表，并转换为FacilityInfo对象列表，设置到RoomDetailVo对象中
        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);
        if (CollectionUtils.isEmpty(roomFacilities)) {
            return null;
        }

        // 提取房间设施列表中的设施ID，用于后续查询设施信息
        List<Long> facilityInfoIds = roomFacilities.stream()
                .map(RoomFacility::getFacilityId)
                .toList();

        // 根据设施ID列表查询并返回设施信息列表
        return listByIds(facilityInfoIds);
    }
}
