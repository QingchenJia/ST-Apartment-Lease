package com.atguigu.lease.web.app.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.web.app.service.SmsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {
    @Resource
    private Client client;

    /**
     * 重写获取验证码方法
     * 生成随机验证码，并通过阿里云短信服务发送到指定手机
     *
     * @param phone 接收验证码的手机号码
     * @return 发送的验证码字符串
     * @throws Exception 如果发送短信过程中发生错误，则抛出异常
     */
    @Override
    public String getCode(String phone) throws Exception {
        // 生成4位随机验证码
        String code = CodeUtil.getRandomCode(4);

        // 创建发送短信请求对象，并设置相关参数
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        // 设置接收短信的手机号码
        sendSmsRequest.setPhoneNumbers(phone)
                // 设置短信签名名称
                .setSignName("阿里云短信测试")
                // 设置短信模板代码
                .setTemplateCode("SMS_154950909")
                // 设置短信模板参数，此处是将验证码放入模板
                .setTemplateParam("{\"code\":\"%s\"}".formatted(code));

        // 使用客户端发送短信请求
        client.sendSms(sendSmsRequest);

        // 返回生成的验证码
        return code;
    }
}
