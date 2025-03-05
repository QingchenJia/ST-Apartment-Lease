package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.web.app.mapper.BrowsingHistoryMapper;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory> implements BrowsingHistoryService {
    @Resource
    @Lazy
    private RoomInfoService roomInfoService;

    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    private ApartmentInfoService apartmentInfoService;

    @Resource
    private ProvinceInfoService provinceInfoService;

    @Resource
    private CityInfoService cityInfoService;

    @Resource
    private DistrictInfoService districtInfoService;

    /**
     * 根据指定的当前页和大小分页查询浏览历史项
     * 此方法首先创建一个分页对象，然后设置查询条件为按浏览时间降序排序，
     * 执行分页查询后，将查询到的浏览历史记录转换为历史项视图对象（HistoryItemVo），
     * 并 enriched 有关的房间、公寓、区域、城市和省级信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 返回一个分页对象，其中包含历史项视图对象
     */
    @Override
    public IPage<HistoryItemVo> pageItem(long current, long size) {
        // 创建分页对象
        Page<BrowsingHistory> page = new Page<>(current, size);

        // 获取当前登录用户
        LoginUser loginUser = LoginUserHolder.getLoginUser();

        // 创建查询条件，按浏览时间降序排序
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, loginUser.getId())
                .orderByDesc(BrowsingHistory::getBrowseTime);
        // 执行分页查询
        page(page, queryWrapper);

        // 获取查询结果列表
        List<BrowsingHistory> browsingHistories = page.getRecords();
        // 将浏览历史记录转换为历史项视图对象，并 enriched 相关信息
        List<HistoryItemVo> historyItemVos = browsingHistories.stream()
                .map(browsingHistory -> {
                    HistoryItemVo historyItemVo = new HistoryItemVo();
                    BeanUtils.copyProperties(browsingHistory, historyItemVo);

                    // 获取并设置房间信息
                    RoomInfo roomInfo = roomInfoService.getById(browsingHistory.getRoomId());
                    historyItemVo.setRoomNumber(roomInfo.getRoomNumber());
                    historyItemVo.setRent(roomInfo.getRent());

                    // 获取并设置房间图片信息
                    List<GraphVo> graphVos = graphInfoService.listByRoomId(browsingHistory.getRoomId());
                    if (!CollectionUtils.isEmpty(graphVos)) {
                        historyItemVo.setRoomGraphVoList(graphVos);
                    }

                    // 获取并设置公寓信息
                    ApartmentInfo apartmentInfo = apartmentInfoService.getById(roomInfo.getApartmentId());
                    historyItemVo.setApartmentName(apartmentInfo.getName());

                    // 获取并设置区域信息
                    DistrictInfo districtInfo = districtInfoService.getById(apartmentInfo.getDistrictId());
                    historyItemVo.setDistrictName(districtInfo.getName());

                    // 获取并设置城市信息
                    CityInfo cityInfo = cityInfoService.getById(districtInfo.getCityId());
                    historyItemVo.setCityName(cityInfo.getName());

                    // 获取并设置省级信息
                    ProvinceInfo provinceInfo = provinceInfoService.getById(cityInfo.getProvinceId());
                    historyItemVo.setProvinceName(provinceInfo.getName());

                    return historyItemVo;
                })
                .toList();

        // 创建结果分页对象
        Page<HistoryItemVo> resultPage = new Page<>();
        // 复制分页属性
        BeanUtils.copyProperties(page, resultPage, "records");
        // 设置历史项视图对象列表
        resultPage.setRecords(historyItemVos);

        // 返回结果分页对象
        return resultPage;
    }

    /**
     * 记录用户浏览房间的历史
     * 此方法使用异步执行和事务管理，以提高性能和确保数据一致性
     *
     * @param roomId 房间ID
     * @param userId 用户ID
     */
    @Override
    @Async
    @Transactional
    public void recordHistory(Long roomId, Long userId) {
        // 创建查询条件，用于检查用户是否已经浏览过该房间
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId)
                .eq(BrowsingHistory::getRoomId, roomId);

        // 根据查询条件获取浏览历史记录
        BrowsingHistory browsingHistory = getOne(queryWrapper);
        if (browsingHistory == null) {
            // 如果没有找到浏览记录，则创建新的浏览历史对象
            browsingHistory = new BrowsingHistory();
            browsingHistory.setRoomId(roomId);
            browsingHistory.setUserId(userId);
        }

        // 更新或设置浏览时间
        browsingHistory.setBrowseTime(new Date());
        // 保存或更新浏览历史记录
        saveOrUpdate(browsingHistory);
    }
}
