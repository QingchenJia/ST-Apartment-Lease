package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.model.entity.RoomAttrValue;
import com.atguigu.lease.web.admin.mapper.AttrValueMapper;
import com.atguigu.lease.web.admin.service.AttrKeyService;
import com.atguigu.lease.web.admin.service.AttrValueService;
import com.atguigu.lease.web.admin.service.RoomAttrValueService;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
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
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class AttrValueServiceImpl extends ServiceImpl<AttrValueMapper, AttrValue> implements AttrValueService {
    @Resource
    private RoomAttrValueService roomAttrValueService;

    @Resource
    private AttrKeyService attrKeyService;

    /**
     * 根据房间ID获取房间属性值列表
     *
     * @param id 房间ID，用于查询与该房间相关的所有属性值
     * @return 返回一个AttrValueVo对象列表，包含与指定房间相关的所有属性值信息如果找不到相关属性值，则返回null
     */
    @Override
    public List<AttrValueVo> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID相关的房间属性值列表
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueQueryWrapper = new LambdaQueryWrapper<>();
        roomAttrValueQueryWrapper.eq(RoomAttrValue::getRoomId, id);

        // 执行查询，获取与房间ID相关的房间属性值列表
        List<RoomAttrValue> roomAttrValues = roomAttrValueService.list(roomAttrValueQueryWrapper);
        // 检查查询结果是否为空，如果为空，则直接返回null
        if (CollectionUtils.isEmpty(roomAttrValues)) {
            return null;
        }

        // 根据查询条件获取房间属性值列表，并转换为AttrValueVo对象列表，设置到RoomDetailVo对象中
        return roomAttrValues.stream()
                .map(roomAttrValue -> {
                    // 根据属性值ID获取属性值对象
                    AttrValue attrValue = getById(roomAttrValue.getAttrValueId());
                    // 根据属性键ID获取属性键对象
                    AttrKey attrKey = attrKeyService.getById(attrValue.getAttrKeyId());

                    // 创建AttrValueVo对象，并从AttrValue对象中复制属性
                    AttrValueVo attrValueVo = new AttrValueVo();
                    BeanUtils.copyProperties(attrValue, attrValueVo);
                    // 设置属性键名称
                    attrValueVo.setAttrKeyName(attrKey.getName());

                    // 返回AttrValueVo对象
                    return attrValueVo;
                })
                .toList();
    }
}
