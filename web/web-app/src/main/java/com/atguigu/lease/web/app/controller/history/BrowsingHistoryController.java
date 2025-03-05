package com.atguigu.lease.web.app.controller.history;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "浏览历史管理")
@RequestMapping("/app/history")
public class BrowsingHistoryController {
    @Resource
    private BrowsingHistoryService browsingHistoryService;

    @Operation(summary = "获取浏览历史")
    @GetMapping("/pageItem")
    private Result<IPage<HistoryItemVo>> page(@RequestParam long current, @RequestParam long size) {
        IPage<HistoryItemVo> historyItemVoIPage = browsingHistoryService.pageItem(current, size);
        return Result.ok(historyItemVoIPage);
    }
}
