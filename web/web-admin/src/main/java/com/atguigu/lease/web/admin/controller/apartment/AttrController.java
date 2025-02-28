package com.atguigu.lease.web.admin.controller.apartment;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.web.admin.service.AttrKeyService;
import com.atguigu.lease.web.admin.service.AttrValueService;
import com.atguigu.lease.web.admin.vo.attr.AttrKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "房间属性管理")
@RestController
@RequestMapping("/admin/attr")
public class AttrController {
    @Resource
    private AttrKeyService attrKeyService;

    @Resource
    private AttrValueService attrValueService;

    @Operation(summary = "新增或更新属性名称")
    @PostMapping("/key/saveOrUpdate")
    public Result<?> saveOrUpdateAttrKey(@RequestBody AttrKey attrKey) {
        attrKeyService.saveOrUpdate(attrKey);
        return Result.ok();
    }

    @Operation(summary = "新增或更新属性值")
    @PostMapping("/value/saveOrUpdate")
    public Result<?> saveOrUpdateAttrValue(@RequestBody AttrValue attrValue) {
        attrValueService.saveOrUpdate(attrValue);
        return Result.ok();
    }

    @Operation(summary = "查询全部属性名称和属性值列表")
    @GetMapping("/list")
    public Result<List<AttrKeyVo>> listAttrInfo() {
        List<AttrKeyVo> attrKeyVos = attrKeyService.listAttrInfo();
        return Result.ok(attrKeyVos);
    }

    @Operation(summary = "根据id删除属性名称")
    @DeleteMapping("/key/deleteById")
    @Transactional
    public Result<?> removeAttrKeyById(@RequestParam Long attrKeyId) {
        attrKeyService.removeById(attrKeyId);

        LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttrValue::getAttrKeyId, attrKeyId);
        attrValueService.remove(queryWrapper);

        return Result.ok();
    }

    @Operation(summary = "根据id删除属性值")
    @DeleteMapping("/value/deleteById")
    public Result<?> removeAttrValueById(@RequestParam Long id) {
        attrValueService.removeById(id);
        return Result.ok();
    }
}
