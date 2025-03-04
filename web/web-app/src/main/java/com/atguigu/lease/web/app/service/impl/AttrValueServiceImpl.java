package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.model.entity.RoomAttrValue;
import com.atguigu.lease.web.app.mapper.AttrValueMapper;
import com.atguigu.lease.web.app.service.AttrKeyService;
import com.atguigu.lease.web.app.service.AttrValueService;
import com.atguigu.lease.web.app.service.RoomAttrValueService;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【attr_value(房间基本属性值表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue> implements AttrValueService {
    @Resource
    private RoomAttrValueService roomAttrValueService;

    @Resource
    private AttrKeyService attrKeyService;

    /**
     * 根据房间ID获取属性值列表
     *
     * @param id 房间ID，用于查询与房间关联的属性值
     * @return 返回一个AttrValueVo对象的列表，包含房间的属性值信息如果找不到相关属性值，则返回null
     */
    @Override
    public List<AttrValueVo> listByRoomId(Long id) {
        // 创建查询条件，用于查询与房间ID匹配的房间属性值
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);

        // 执行查询，获取与房间ID关联的房间属性值列表
        List<RoomAttrValue> roomAttrValues = roomAttrValueService.list(roomAttrValueQueryWrapper);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(roomAttrValues)) {
            return null;
        }

        // 将查询到的房间属性值列表转换为AttrValueVo对象列表
        return roomAttrValues.stream()
                .map(roomAttrValue -> {
                    // 创建一个新的AttrValueVo对象，用于存储转换后的属性值信息
                    AttrValueVo attrValueVo = new AttrValueVo();

                    // 根据属性值ID获取属性值详细信息，并将相关信息复制到AttrValueVo对象中
                    AttrValue attrValue = getById(roomAttrValue.getAttrValueId());
                    BeanUtils.copyProperties(attrValue, attrValueVo);

                    // 获取属性键信息，并设置到AttrValueVo对象中
                    AttrKey attrKey = attrKeyService.getById(attrValue.getAttrKeyId());
                    attrValueVo.setAttrKeyName(attrKey.getName());

                    // 返回转换后的AttrValueVo对象
                    return attrValueVo;
                })
                .toList();
    }
}
