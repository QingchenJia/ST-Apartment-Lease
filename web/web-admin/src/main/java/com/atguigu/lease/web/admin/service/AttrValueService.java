package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【attr_value(房间基本属性值表)】的数据库操作Service
 * &#064;createDate  2023-07-24 15:48:00
 */
public interface AttrValueService extends IService<AttrValue> {
    List<AttrValueVo> listByRoomId(Long id);
}
