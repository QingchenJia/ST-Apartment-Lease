package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.web.admin.mapper.AttrKeyMapper;
import com.atguigu.lease.web.admin.service.AttrKeyService;
import com.atguigu.lease.web.admin.service.AttrValueService;
import com.atguigu.lease.web.admin.vo.attr.AttrKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【attr_key(房间基本属性表)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class AttrKeyServiceImpl extends ServiceImpl<AttrKeyMapper, AttrKey> implements AttrKeyService {
    @Resource
    private AttrValueService attrValueService;

    /**
     * 重写父类方法以获取属性信息列表
     * <p>
     * 此方法首先查询所有属性键（AttrKey），然后为每个属性键查询其对应的属性值（AttrValue）
     * 最后，将属性键及其对应的属性值封装到AttrKeyVo对象中，并返回AttrKeyVo对象列表
     *
     * @return 属性信息列表，每个元素包含一个属性键及其对应的属性值列表
     */
    @Override
    public List<AttrKeyVo> listAttrInfo() {
        // 初始化属性信息列表
        List<AttrKeyVo> attrKeyVos = new ArrayList<>();

        // 查询所有属性键
        List<AttrKey> attrKeys = list();

        // 遍历每个属性键，查询并封装其对应的属性值
        for (AttrKey attrKey : attrKeys) {
            // 创建查询条件，用于查询与当前属性键关联的属性值
            LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrValue::getAttrKeyId, attrKey.getId());
            // 执行查询，获取属性值列表
            List<AttrValue> attrValues = attrValueService.list(queryWrapper);

            // 创建属性键视图对象，并将属性键的属性复制到视图对象中
            AttrKeyVo attrKeyVo = new AttrKeyVo();
            BeanUtils.copyProperties(attrKey, attrKeyVo);
            // 设置属性键视图对象的属性值列表
            attrKeyVo.setAttrValueList(attrValues);

            // 将封装好的属性键视图对象添加到属性信息列表中
            attrKeyVos.add(attrKeyVo);
        }

        // 返回属性信息列表
        return attrKeyVos;
    }
}
