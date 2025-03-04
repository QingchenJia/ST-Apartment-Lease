package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import com.atguigu.lease.web.app.service.GraphInfoService;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【graph_info(图片信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo> implements GraphInfoService {
    /**
     * 根据房间ID列出图形信息
     * 此方法通过房间ID查询相关的图形信息，并将其转换为图形视图对象列表返回
     * 如果没有找到任何图形信息，则返回null
     *
     * @param id 房间ID，用于查询图形信息
     * @return 包含图形视图对象的列表，如果找不到则返回null
     */
    @Override
    public List<GraphVo> listByRoomId(Long id) {
        // 创建查询条件，筛选出与指定房间ID相关的图形信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapperOfRoom = new LambdaQueryWrapper<>();
        graphInfoQueryWrapperOfRoom.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.ROOM);

        // 执行查询，获取与房间相关的图形信息列表
        List<GraphInfo> graphInfosOfRoom = list(graphInfoQueryWrapperOfRoom);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(graphInfosOfRoom)) {
            return null;
        }

        // 将查询到的图形信息列表转换为图形视图对象列表，并返回
        return graphInfosOfRoom.stream()
                .map(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    return graphVo;
                })
                .toList();
    }

    /**
     * 根据公寓ID列出所有相关的图表信息
     * 此方法首先根据给定的公寓ID查询数据库中所有与该公寓相关的图表信息，
     * 如果存在相关图表信息，则将这些信息转换为GraphVo对象列表并返回
     * 如果不存在任何图表信息，则返回null
     *
     * @param id 公寓的ID，用于查询与该公寓相关的图表信息
     * @return 包含所有与指定公寓相关的图表信息的列表，如果无相关信息则返回null
     */
    @Override
    public List<GraphVo> listByApartmentId(Long id) {
        // 创建查询条件，查询与指定公寓ID相关的图表信息
        LambdaQueryWrapper<GraphInfo> graphInfoQueryWrapperOfApartment = new LambdaQueryWrapper<>();
        graphInfoQueryWrapperOfApartment.eq(GraphInfo::getItemId, id)
                .eq(GraphInfo::getItemType, ItemType.APARTMENT);

        // 执行查询，获取与指定公寓相关的图表信息列表
        List<GraphInfo> graphInfosOfApartment = list(graphInfoQueryWrapperOfApartment);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(graphInfosOfApartment)) {
            return null;
        }

        // 将查询到的图表信息列表转换为GraphVo对象列表，并返回
        return graphInfosOfApartment.stream()
                .map(graphInfo -> {
                    GraphVo graphVo = new GraphVo();
                    BeanUtils.copyProperties(graphInfo, graphVo);
                    return graphVo;
                })
                .toList();
    }
}
