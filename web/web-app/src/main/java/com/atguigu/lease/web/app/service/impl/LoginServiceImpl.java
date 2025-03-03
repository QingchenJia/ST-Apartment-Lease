package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SmsService smsService;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 发送登录验证码
     * <p>
     * 根据手机号生成并发送验证码，同时存储到Redis中，以支持后续的验证流程
     * 该方法重写了父类的方法，提供了具体的验证码发送逻辑
     *
     * @param phone 手机号，用于接收验证码
     * @throws Exception 如果发送验证码过程中出现异常，则抛出此异常
     */
    @Override
    public void getCode(String phone) throws Exception {
        // 构造Redis键值，用于存储该手机号对应的验证码
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;

        // 检查Redis中是否已存在该手机号的验证码
        if (stringRedisTemplate.hasKey(key)) {
            // 获取验证码的剩余有效时间
            Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            // 判断是否在允许重新发送验证码的时间范围内
            if (RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC - expire < RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC) {
                // 如果频繁发送验证码，抛出异常提示用户
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }

        // 调用短信服务获取验证码
        String code = smsService.getCode(phone);

        // 将验证码存储到Redis中，并设置过期时间
        stringRedisTemplate.opsForValue()
                .set(key, code, RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
    }

    /**
     * 登录功能实现方法
     *
     * @param loginVo 登录视图对象，包含用户输入的手机号和验证码
     * @return 用户登录成功后返回的令牌（Token）
     * @throws LeaseException 当手机号或验证码为空、验证码错误、或用户账户被禁用时抛出异常
     */
    @Override
    public String login(LoginVo loginVo) {
        // 获取用户输入的手机号
        String phone = loginVo.getPhone();
        // 检查手机号是否为空，为空则抛出异常
        if (phone == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        // 获取用户输入的验证码
        String code = loginVo.getCode();
        // 检查验证码是否为空，为空则抛出异常
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        // 构造Redis中验证码的键
        String key = RedisConstant.APP_LOGIN_PREFIX + phone;
        // 从Redis中获取对应手机号的正确验证码
        String rightCode = stringRedisTemplate.opsForValue()
                .get(key);

        // 检查Redis中是否存在该手机号对应的验证码，不存在则抛出异常
        if (rightCode == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 比较用户输入的验证码与Redis中存储的验证码是否一致，不一致则抛出异常
        if (!code.equals(rightCode)) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        // 构造查询用户信息的条件
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone, phone);
        // 根据手机号查询用户信息
        UserInfo userInfo = userInfoService.getOne(queryWrapper);

        // 如果查询不到用户信息，则创建新用户
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setPhone(phone);
            userInfo.setNickname("用户-" + phone.substring(5));
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfoService.save(userInfo);
        } else {
            // 如果用户账户被禁用，则抛出异常
            if (BaseStatus.DISABLE.equals(userInfo.getStatus())) {
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }

        // 生成并返回用户登录令牌（Token）
        return JwtUtil.createToken(userInfo.getId(), userInfo.getPhone());
    }

    /**
     * 获取当前登录用户的详细信息
     * <p>
     * 此方法首先从登录用户持有器中获取当前登录用户的信息，
     * 然后根据登录用户的ID从用户信息服务中获取用户详情，
     * 并将这些信息封装到一个用户信息视图对象中返回
     *
     * @return UserInfoVo 用户信息视图对象，包含用户详细信息
     */
    @Override
    public UserInfoVo info() {
        // 获取当前登录用户信息
        LoginUser loginUser = LoginUserHolder.getLoginUser();

        // 创建用户信息视图对象实例
        UserInfoVo userInfoVo = new UserInfoVo();

        // 通过用户ID获取用户详细信息
        UserInfo userInfo = userInfoService.getById(loginUser.getId());

        // 将用户详细信息复制到视图对象中
        BeanUtils.copyProperties(userInfo, userInfoVo);

        // 返回用户信息视图对象
        return userInfoVo;
    }
}
