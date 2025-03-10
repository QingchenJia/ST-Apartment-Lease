package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.LabelInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【label_info(标签信息表)】的数据库操作Service
 * &#064;createDate  2023-07-26 11:12:39
 */
public interface LabelInfoService extends IService<LabelInfo> {
    List<LabelInfo> listByRoomId(Long id);

    List<LabelInfo> listByApartmentId(Long id);
}
