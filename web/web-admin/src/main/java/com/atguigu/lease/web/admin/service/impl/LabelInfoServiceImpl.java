package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.ApartmentLabel;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.RoomLabel;
import com.atguigu.lease.web.admin.mapper.LabelInfoMapper;
import com.atguigu.lease.web.admin.service.ApartmentLabelService;
import com.atguigu.lease.web.admin.service.LabelInfoService;
import com.atguigu.lease.web.admin.service.RoomLabelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【label_info(标签信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo> implements LabelInfoService {
    @Resource
    private ApartmentLabelService apartmentLabelService;

    @Resource
    private RoomLabelService roomLabelService;

    /**
     * 根据公寓ID查询标签信息列表
     *
     * @param id 公寓ID，用于查询与公寓关联的标签信息
     * @return 返回标签信息列表，如果公寓没有关联的标签信息，则返回null
     */
    @Override
    public List<LabelInfo> listByApartmentId(Long id) {
        // 查询公寓的标签信息
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        List<ApartmentLabel> apartmentLabels = apartmentLabelService.list(apartmentLabelQueryWrapper);

        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(apartmentLabels)) {
            return null;
        }

        // 根据公寓标签信息获取具体的标签详情，并设置到公寓详情对象中
        List<Long> labelInfoIds = apartmentLabels.stream()
                .map(ApartmentLabel::getLabelId)
                .toList();

        // 根据标签ID列表查询并返回标签信息列表
        return listByIds(labelInfoIds);
    }

    /**
     * 根据房间ID获取标签信息列表
     *
     * @param id 房间ID，用于查询与该房间相关的标签信息
     * @return 返回LabelInfo对象的列表，每个对象包含一个房间标签的详细信息如果找不到相关标签，则返回null
     */
    @Override
    public List<LabelInfo> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID相关的房间标签列表
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);

        // 根据查询条件获取房间标签列表，并转换为LabelInfo对象列表，设置到RoomDetailVo对象中
        List<RoomLabel> roomLabels = roomLabelService.list(roomLabelQueryWrapper);
        if (CollectionUtils.isEmpty(roomLabels)) {
            return null;
        }

        // 提取房间标签的标签ID，用于后续获取标签信息
        List<Long> labelInfoIds = roomLabels.stream()
                .map(RoomLabel::getLabelId)
                .toList();

        // 根据标签ID列表获取标签信息列表
        return listByIds(labelInfoIds);
    }
}
