package com.atguigu.lease.web.app.controller.login;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录管理")
@RestController
@RequestMapping("/app")
public class LoginController {
    @Resource
    private LoginService loginService;

    @GetMapping("/login/getCode")
    @Operation(summary = "获取短信验证码")
    public Result<?> getCode(@RequestParam String phone) throws Exception {
        loginService.getCode(phone);
        return Result.ok();
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<String> login(@RequestBody LoginVo loginVo) {
        String jwt = loginService.login(loginVo);
        return Result.ok(jwt);
    }

    @GetMapping("/info")
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVo> info() {
        UserInfoVo userInfoVo = loginService.info();
        return Result.ok(userInfoVo);
    }
}
