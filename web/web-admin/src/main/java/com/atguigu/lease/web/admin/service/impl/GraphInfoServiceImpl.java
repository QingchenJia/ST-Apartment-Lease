package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.GraphInfoMapper;
import com.atguigu.lease.web.admin.service.GraphInfoService;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【graph_info(图片信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo> implements GraphInfoService {
    /**
     * 根据公寓ID列出相关的图片信息
     * 此方法首先根据提供的公寓ID查询数据库中相关的图片信息，然后将查询到的数据转换为GraphVo对象列表返回
     * 如果没有找到相关的图片信息，则返回null
     *
     * @param id 公寓的ID，用于查询相关的图片信息
     * @return 包含公寓相关图片信息的GraphVo对象列表，如果找不到相关图片信息则返回null
     */
    @Override
    public List<GraphVo> listByApartmentId(Long id) {
        // 查询与公寓相关的图片信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);
        List<GraphInfo> graphInfos = list(graphInfoQueryWrapper);

        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(graphInfos)) {
            return null;
        }

        // 将查询到的图片信息转换为GraphVo对象，便于后续处理和传输
        return graphInfos.stream()
                .map(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    return graphVo;
                })
                .toList();
    }

    /**
     * 根据房间ID列出图形信息
     * <p>
     * 此方法通过接收一个房间ID，查询并返回与该房间相关的图形信息列表
     * 它首先构建一个查询条件，然后根据这个条件从数据库中获取图形信息列表，
     * 如果列表为空，则返回null；否则，将列表中的每个图形信息转换为GraphVo对象，并返回这个列表
     *
     * @param id 房间ID，用于查询与该房间相关的图形信息
     * @return 返回一个GraphVo对象列表，包含与指定房间相关的所有图形信息如果无相关信息，则返回null
     */
    @Override
    public List<GraphVo> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID和项类型为房间相关的图形信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoQueryWrapper.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);

        // 执行查询，获取图形信息列表
        List<GraphInfo> graphInfos = list(graphInfoQueryWrapper);
        // 如果查询结果为空，返回null
        if (CollectionUtils.isEmpty(graphInfos)) {
            return null;
        }

        // 根据查询条件获取图形信息列表，并转换为GraphVo对象列表，设置到RoomDetailVo对象中
        return graphInfos.stream()
                .map(graphInfo -> {
                    // 创建一个新的GraphVo对象，并将图形信息的属性复制到该对象中
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    // 返回转换后的GraphVo对象
                    return graphVo;
                })
                // 将流转换为列表，并返回
                .toList();
    }
}
