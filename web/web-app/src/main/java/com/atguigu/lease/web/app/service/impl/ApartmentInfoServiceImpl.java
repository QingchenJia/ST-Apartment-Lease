package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.service.*;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

/**
 * @author liubo
 * &#064;description  针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * &#064;createDate  2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo> implements ApartmentInfoService {
    @Resource
    private LabelInfoService labelInfoService;

    @Resource
    private GraphInfoService graphInfoService;

    @Resource
    @Lazy
    private RoomInfoService roomInfoService;

    @Resource
    private FacilityInfoService facilityInfoService;

    /**
     * 根据公寓ID获取公寓详细信息
     * <p>
     * 此方法首先根据提供的公寓ID获取公寓的基本信息，然后分别获取该公寓的标签信息、图表信息和设施信息，
     * 最后获取该公寓下所有房间的信息，以确定最小租金信息所有这些信息都被封装到一个ApartmentDetailVo对象中，
     * 该对象最终被返回给调用者
     *
     * @param id 公寓ID，用于查询公寓详细信息
     * @return ApartmentDetailVo 包含公寓详细信息的对象
     */
    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();

        // 获取公寓基本信息
        ApartmentInfo apartmentInfo = getById(id);
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);

        // 获取公寓的标签信息，并设置到ApartmentItemVo对象中
        List<LabelInfo> labelInfosOfApartment = labelInfoService.listByApartmentId(apartmentInfo.getId());
        if (!CollectionUtils.isEmpty(labelInfosOfApartment)) {
            apartmentDetailVo.setLabelInfoList(labelInfosOfApartment);
        }

        // 获取公寓的图表信息，并设置到ApartmentItemVo对象中
        List<GraphVo> graphVosOfApartment = graphInfoService.listByApartmentId(apartmentInfo.getId());
        if (!CollectionUtils.isEmpty(graphVosOfApartment)) {
            apartmentDetailVo.setGraphVoList(graphVosOfApartment);
        }

        // 获取公寓的设施信息，并设置到ApartmentItemVo对象中
        List<FacilityInfo> facilityInfos = facilityInfoService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(facilityInfos)) {
            apartmentDetailVo.setFacilityInfoList(facilityInfos);
        }

        // 获取公寓下所有房间信息，以确定最小租金，并设置到ApartmentItemVo对象中
        List<RoomInfo> roomInfos = roomInfoService.listByApartmentId(id);
        if (!CollectionUtils.isEmpty(roomInfos)) {
            RoomInfo roomInfoWithMinRent = roomInfos.stream()
                    .min(Comparator.comparing(RoomInfo::getRent))
                    .get();

            apartmentDetailVo.setMinRent(roomInfoWithMinRent.getRent());
        }

        return apartmentDetailVo;
    }
}
