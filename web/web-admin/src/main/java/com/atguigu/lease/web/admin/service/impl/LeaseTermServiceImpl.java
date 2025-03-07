package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.model.entity.RoomLeaseTerm;
import com.atguigu.lease.web.admin.mapper.LeaseTermMapper;
import com.atguigu.lease.web.admin.service.LeaseTermService;
import com.atguigu.lease.web.admin.service.RoomLeaseTermService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_term(租期)】的数据库操作Service实现
 * &#064;createDate  2023-07-24 15:48:00
 */
@Service
public class LeaseTermServiceImpl extends ServiceImpl<LeaseTermMapper, LeaseTerm> implements LeaseTermService {
    @Resource
    private RoomLeaseTermService roomLeaseTermService;

    /**
     * 根据房间ID获取租赁期限列表
     *
     * @param id 房间ID，用于查询与该房间相关的租赁期限
     * @return 返回LeaseTerm对象列表，表示与指定房间ID相关的所有租赁期限如果找不到相关租赁期限，则返回null
     */
    @Override
    public List<LeaseTerm> listByRoomId(Long id) {
        // 创建查询条件，用于获取与房间ID相关的房间租赁期限列表
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermQueryWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermQueryWrapper.eq(RoomLeaseTerm::getRoomId, id);

        // 根据查询条件获取房间租赁期限列表，并转换为LeaseTerm对象列表，设置到RoomDetailVo对象中
        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermService.list(roomLeaseTermQueryWrapper);
        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(roomLeaseTerms)) {
            return null;
        }

        // 提取所有租赁期限ID，以便后续查询具体的LeaseTerm对象
        List<Long> leaseTermIds = roomLeaseTerms.stream()
                .map(RoomLeaseTerm::getLeaseTermId)
                .toList();

        // 根据提取的租赁期限ID列表，获取并返回LeaseTerm对象列表
        return listByIds(leaseTermIds);
    }
}
