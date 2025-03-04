package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.RoomFacility;
import com.atguigu.lease.web.app.mapper.FacilityInfoMapper;
import com.atguigu.lease.web.app.service.FacilityInfoService;
import com.atguigu.lease.web.app.service.RoomFacilityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【facility_info(配套信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class FacilityInfoServiceImpl extends ServiceImpl<FacilityInfoMapper, FacilityInfo> implements FacilityInfoService {
    @Resource
    private RoomFacilityService roomFacilityService;

    /**
     * 根据房间ID列出设施信息
     * <p>
     * 此方法通过查询与特定房间ID关联的所有设施信息，首先构造查询条件以获取房间设施对象列表，
     * 然后提取这些对象的设施ID，最后根据这些ID查询并返回对应的设施信息列表
     *
     * @param id 房间的唯一标识符
     * @return 返回包含设施信息的列表，如果找不到相关设施信息，则返回null
     */
    @Override
    public List<FacilityInfo> listByRoomId(Long id) {
        // 构造查询条件，筛选出与给定房间ID匹配的房间设施
        LambdaQueryWrapper<RoomFacility> roomFacilityQueryWrapper = new LambdaQueryWrapper<>();
        roomFacilityQueryWrapper.eq(RoomFacility::getRoomId, id);

        // 执行查询，获取与房间ID关联的房间设施列表
        List<RoomFacility> roomFacilities = roomFacilityService.list(roomFacilityQueryWrapper);
        // 如果查询结果为空，直接返回null
        if (CollectionUtils.isEmpty(roomFacilities)) {
            return null;
        }

        // 从查询结果中提取所有设施ID，准备后续根据这些ID查询设施信息
        List<Long> facilityInfoIds = roomFacilities.stream()
                .map(RoomFacility::getFacilityId)
                .toList();

        // 根据提取的设施ID列表，查询并返回对应的设施信息列表
        return listByIds(facilityInfoIds);
    }
}
