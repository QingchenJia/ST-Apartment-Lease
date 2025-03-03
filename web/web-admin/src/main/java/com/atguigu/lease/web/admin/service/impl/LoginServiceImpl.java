package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.service.SystemUserService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SystemUserService systemUserService;

    /**
     * 生成并返回验证码对象
     * <p>
     * 本方法首先创建一个特定规格的验证码对象，然后生成验证码文本，
     * 并将验证码文本及其对应的关键字存储到Redis中，最后返回包含验证码图片和关键字的验证码对象
     *
     * @return CaptchaVo 包含验证码图片和关键字的验证码对象
     */
    @Override
    public CaptchaVo getCaptcha() {
        // 创建一个宽度为130像素，高度为48像素，包含4个字符的验证码对象
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);

        // 生成一个唯一的键，用于存储验证码信息
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();

        // 生成验证码文本
        String code = specCaptcha.text();

        // 将验证码的关键字和文本存储到Redis中，并设置过期时间
        stringRedisTemplate.opsForValue()
                .set(key, code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);

        // 返回包含验证码图片（以Base64编码）和关键字的验证码对象
        return new CaptchaVo(specCaptcha.toBase64(), key);
    }

    /**
     * 登录功能实现方法
     *
     * @param loginVo 登录视图对象，包含用户输入的登录信息
     * @return 登录成功后生成的JWT令牌
     * @throws LeaseException 当验证码缺失、过期、错误，用户名不存在或账户被禁用时抛出异常
     */
    @Override
    public String login(LoginVo loginVo) {
        // 获取用户输入的验证码
        String captchaCode = loginVo.getCaptchaCode();
        // 验证码为空时，抛出异常
        if (captchaCode == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        // 从Redis中获取存储的验证码
        String code = stringRedisTemplate.opsForValue()
                .get(loginVo.getCaptchaKey());
        // 存储的验证码为空时，抛出异常
        if (code == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        // 用户输入的验证码与存储的验证码不匹配时，抛出异常
        if (!captchaCode.equals(code)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        // 查询数据库中是否存在与输入用户名匹配的用户
        LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUser::getUsername, loginVo.getUsername());
        SystemUser systemUser = systemUserService.getOne(queryWrapper);
        // 用户不存在时，抛出异常
        if (systemUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        // 用户账户被禁用时，抛出异常
        if (BaseStatus.DISABLE.equals(systemUser.getStatus())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        // 获取用户输入的密码
        String password = loginVo.getPassword();
        // 输入的密码与数据库中存储的密码不匹配时，抛出异常
        if (!DigestUtils.md5Hex(password).equals(systemUser.getPassword())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        // 登录成功，生成并返回JWT令牌
        return JwtUtil.createToken(systemUser.getId(), systemUser.getUsername());
    }

    /**
     * 根据用户ID获取用户信息
     * <p>
     * 该方法首先创建一个SystemUserInfoVo对象，然后通过systemUserService根据提供的用户ID获取SystemUser对象
     * 随后，使用BeanUtils.copyProperties方法将SystemUser对象的属性复制到SystemUserInfoVo对象中
     * 这样做是为了提供一个包含用户信息的视图对象，而不需要直接暴露SystemUser实体
     *
     * @param id 用户ID，用于查询用户信息
     * @return 返回包含用户信息的SystemUserInfoVo对象
     */
    @Override
    public SystemUserInfoVo info(Long id) {
        // 创建一个SystemUserInfoVo对象来存储用户信息
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();

        // 通过systemUserService根据用户ID获取用户实体对象
        SystemUser systemUser = systemUserService.getById(id);

        // 将用户实体对象的属性复制到视图对象中，以便于展示层使用
        BeanUtils.copyProperties(systemUser, systemUserInfoVo);

        // 返回填充好的用户信息视图对象
        return systemUserInfoVo;
    }
}
