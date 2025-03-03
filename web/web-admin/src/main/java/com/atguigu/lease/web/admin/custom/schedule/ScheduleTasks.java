package com.atguigu.lease.web.admin.custom.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTasks {
    @Resource
    private LeaseAgreementService leaseAgreementService;

    /**
     * 定时检查租赁协议的状态
     * <p>
     * 本方法通过定时任务每天午夜12点自动执行，旨在更新租赁协议的状态
     * 如果租赁协议的结束日期小于当前日期，并且状态为已签订或正在退租
     * 则自动将状态更新为已过期
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus() {
        // 创建更新条件构造器
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        // 设置更新条件：租赁结束日期小于当前日期
        updateWrapper.le(LeaseAgreement::getLeaseEndDate, new Date())
                // 且状态为已签订或正在退租
                .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING)
                // 更新状态为已过期
                .set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);

        // 执行更新操作
        leaseAgreementService.update(updateWrapper);
    }
}
