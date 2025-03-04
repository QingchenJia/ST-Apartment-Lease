package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentLabel;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.RoomLabel;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import com.atguigu.lease.web.app.service.ApartmentLabelService;
import com.atguigu.lease.web.app.service.LabelInfoService;
import com.atguigu.lease.web.app.service.RoomLabelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【label_info(标签信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class LabelInfoServiceImpl extends ServiceImpl<LabelInfoMapper, LabelInfo> implements LabelInfoService {
    @Resource
    private RoomLabelService roomLabelService;

    @Resource
    private ApartmentLabelService apartmentLabelService;

    /**
     * 根据房间ID获取标签信息列表
     * <p>
     * 此方法首先根据给定的房间ID查询与之关联的所有标签，然后根据查询结果中包含的标签ID列表，
     * 进一步查询并返回这些标签的详细信息列表如果未找到与房间ID关联的标签，则返回null
     *
     * @param id 房间ID，用于查询与该房间关联的标签
     * @return 包含标签信息的列表，如果未找到则返回null
     */
    @Override
    public List<LabelInfo> listByRoomId(Long id) {
        // 创建查询条件，筛选出与指定房间ID关联的所有房间标签
        LambdaQueryWrapper<RoomLabel> roomLabelQueryWrapper = new LambdaQueryWrapper<>();
        roomLabelQueryWrapper.eq(RoomLabel::getRoomId, id);

        // 执行查询，获取与房间ID关联的房间标签列表
        List<RoomLabel> roomLabels = roomLabelService.list(roomLabelQueryWrapper);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(roomLabels)) {
            return null;
        }

        // 从查询结果中提取所有标签ID，准备进一步查询这些标签的详细信息
        List<Long> labelIds = roomLabels.stream()
                .map(RoomLabel::getLabelId)
                .toList();

        // 根据提取的标签ID列表，查询并返回这些标签的详细信息列表
        return listByIds(labelIds);
    }

    /**
     * 根据公寓ID列出标签信息
     * <p>
     * 此方法通过公寓ID查询与之关联的所有标签信息它首先查询公寓标签表中与指定公寓ID匹配的记录，
     * 如果找到匹配的记录，就根据这些记录中的标签ID查询具体的标签信息并返回
     *
     * @param id 公寓ID，用于查询与该公寓关联的标签信息
     * @return 返回一个包含标签信息的列表如果指定的公寓ID没有关联的标签信息，则返回null
     */
    @Override
    public List<LabelInfo> listByApartmentId(Long id) {
        // 创建查询条件，查询与指定公寓ID匹配的公寓标签记录
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);

        // 执行查询，获取与指定公寓ID匹配的公寓标签列表
        List<ApartmentLabel> apartmentLabels = apartmentLabelService.list(apartmentLabelQueryWrapper);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(apartmentLabels)) {
            return null;
        }

        // 提取公寓标签列表中的标签ID，用于后续查询具体的标签信息
        List<Long> labelIds = apartmentLabels.stream()
                .map(ApartmentLabel::getLabelId)
                .toList();

        // 根据提取的标签ID列表查询并返回具体的标签信息列表
        return listByIds(labelIds);
    }
}
