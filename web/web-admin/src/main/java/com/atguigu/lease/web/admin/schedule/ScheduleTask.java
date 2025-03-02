package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTask {
    @Resource
    private LeaseAgreementService leaseAgreementService;

    /**
     * 定时检查租赁协议的状态并更新过期状态
     * <p>
     * 该方法使用Spring的定时任务注解@Scheduled，根据cron表达式设定每天午夜12点自动执行
     * 其目的是检查所有已签订或正在撤回的租赁协议，并将那些租赁结束日期早于或等于当前日期的协议状态更新为过期
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkAgreementStatus() {
        // 创建一个Lambda更新包装器，用于构建更新条件
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();

        // 设置更新条件：将状态设置为EXPIRED，条件是租赁结束日期小于等于当前日期，
        // 并且状态当前为SIGNED或WITHDRAWING
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED)
                .le(LeaseAgreement::getLeaseEndDate, new Date())
                .in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);

        // 执行更新操作，自动更新满足条件的记录
        leaseAgreementService.update(updateWrapper);
    }
}
