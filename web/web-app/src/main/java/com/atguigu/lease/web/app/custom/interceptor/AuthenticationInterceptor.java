package com.atguigu.lease.web.app.custom.interceptor;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    /**
     * 在请求处理之前进行拦截处理
     *
     * @param request  请求对象，用于获取请求头中的token信息
     * @param response 响应对象，可用于处理拦截后的响应
     * @param handler  处理器，可以用于判断拦截器链中的下一个处理器
     * @return boolean 恒久化为true，表示请求通过拦截器继续执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头中获取token信息
        String token = request.getHeader("access-token");

        // 解析token并获取claims信息
        Claims claims = JwtUtil.parseToken(token);
        // 从claims中获取用户ID和用户名
        Long id = claims.get("id", Long.class);
        String username = claims.get("username", String.class);
        // 设置当前登录用户信息
        LoginUserHolder.setLoginUser(new LoginUser(id, username));

        // 表示请求通过拦截器继续执行
        return true;
    }

    /**
     * 在请求完成后清除登录用户信息
     * <p>
     * 本方法是拦截器中的一个回调方法，它在请求处理完成后被调用
     * 它的主要作用是清除可能存储在会话或请求范围内的登录用户信息，以避免内存泄漏或信息污染
     *
     * @param request  请求对象，代表当前的HTTP请求
     * @param response 响应对象，代表当前的HTTP响应
     * @param handler  处理当前请求的处理器对象
     * @param ex       请求处理过程中抛出的异常，如果没有异常，则为null
     *                 <p>
     *                 注意：本方法的实现仅清除登录用户信息，并不处理异常情况
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }
}
