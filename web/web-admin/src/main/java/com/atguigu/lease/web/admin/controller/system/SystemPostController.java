package com.atguigu.lease.web.admin.controller.system;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.SystemPost;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.service.SystemPostService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "后台用户岗位管理")
@RequestMapping("/admin/system/post")
public class SystemPostController {
    @Resource
    private SystemPostService systemPostService;

    @Operation(summary = "分页获取岗位信息")
    @GetMapping("/page")
    private Result<IPage<SystemPost>> page(@RequestParam long current, @RequestParam long size) {
        Page<SystemPost> page = new Page<>(current, size);
        systemPostService.page(page);
        return Result.ok(page);
    }

    @Operation(summary = "保存或更新岗位信息")
    @PostMapping("/saveOrUpdate")
    public Result<?> saveOrUpdate(@RequestBody SystemPost systemPost) {
        systemPostService.saveOrUpdate(systemPost);
        return Result.ok();
    }

    @DeleteMapping("/deleteById")
    @Operation(summary = "根据id删除岗位")
    public Result<?> removeById(@RequestParam Long id) {
        systemPostService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/getById")
    @Operation(summary = "根据id获取岗位信息")
    public Result<SystemPost> getById(@RequestParam Long id) {
        SystemPost systemPost = systemPostService.getById(id);
        return Result.ok(systemPost);
    }

    @Operation(summary = "获取全部岗位列表")
    @GetMapping("/list")
    public Result<List<SystemPost>> list() {
        List<SystemPost> systemPosts = systemPostService.list();
        return Result.ok(systemPosts);
    }

    @Operation(summary = "根据岗位id修改状态")
    @PostMapping("/updateStatusByPostId")
    public Result<?> updateStatusByPostId(@RequestParam Long id, @RequestParam BaseStatus status) {
        LambdaUpdateWrapper<SystemPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SystemPost::getStatus, status)
                .eq(SystemPost::getId, id);
        systemPostService.update(updateWrapper);

        return Result.ok();
    }
}
