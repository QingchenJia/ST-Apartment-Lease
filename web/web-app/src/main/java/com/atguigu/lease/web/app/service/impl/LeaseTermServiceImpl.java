package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.model.entity.RoomLeaseTerm;
import com.atguigu.lease.web.app.mapper.LeaseTermMapper;
import com.atguigu.lease.web.app.service.LeaseTermService;
import com.atguigu.lease.web.app.service.RoomLeaseTermService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【lease_term(租期)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class LeaseTermServiceImpl extends ServiceImpl<LeaseTermMapper, LeaseTerm> implements LeaseTermService {
    @Resource
    private RoomLeaseTermService roomLeaseTermService;

    /**
     * 根据房间ID获取租赁条款列表
     *
     * @param id 房间ID，用于查询与该房间关联的租赁条款
     * @return 返回一个LeaseTerm对象列表，这些对象与指定房间ID关联如果找不到任何租赁条款，则返回null
     */
    @Override
    public List<LeaseTerm> listByRoomId(Long id) {
        // 创建查询条件，用于查询具有指定房间ID的所有房间租赁条款
        LambdaQueryWrapper<RoomLeaseTerm> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RoomLeaseTerm::getRoomId, id);
        // 执行查询，获取与指定房间ID关联的所有房间租赁条款
        List<RoomLeaseTerm> roomLeaseTerms = roomLeaseTermService.list(queryWrapper);

        // 如果查询结果为空，则直接返回null
        if (CollectionUtils.isEmpty(roomLeaseTerms)) {
            return null;
        }

        // 提取查询结果中的租赁条款ID，用于后续获取LeaseTerm对象
        List<Long> leaseTermIds = roomLeaseTerms.stream()
                .map(RoomLeaseTerm::getLeaseTermId)
                .toList();

        // 根据提取的租赁条款ID列表，查询并返回对应的LeaseTerm对象列表
        return listByIds(leaseTermIds);
    }
}
