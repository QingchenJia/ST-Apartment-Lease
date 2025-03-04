package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author liubo
 * &#064;description  针对表【room_info(房间信息表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface RoomInfoService extends IService<RoomInfo> {
    IPage<RoomItemVo> pageItem(long current, long size, RoomQueryVo queryVo);

    RoomDetailVo getDetailById(Long id);
}
