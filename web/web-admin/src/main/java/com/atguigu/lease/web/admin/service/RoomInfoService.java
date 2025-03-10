package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author liubo
 * &#064;description  针对表【room_info(房间信息表)】的数据库操作Service
 * &#064;createDate  2023-07-24 15:48:00
 */
public interface RoomInfoService extends IService<RoomInfo> {
    void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo);

    IPage<RoomItemVo> pageItem(long current, long size, RoomQueryVo queryVo);

    RoomDetailVo getDetailById(Long id);

    void removeRoomById(Long id);
}
