package com.atguigu.lease.web.admin.service;

import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【graph_info(图片信息表)】的数据库操作Service
 * &#064;createDate  2023-07-24 15:48:00
 */
public interface GraphInfoService extends IService<GraphInfo> {
    List<GraphVo> listByApartmentId(Long id);

    List<GraphVo> listByRoomId(Long id);
}
