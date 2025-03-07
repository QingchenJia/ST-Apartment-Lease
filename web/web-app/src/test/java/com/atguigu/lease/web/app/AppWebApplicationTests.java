package com.atguigu.lease.web.app;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.atguigu.lease.model.entity.FeeKey;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class AppWebApplicationTests {
    @Resource
    private Client client;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void sendCodeLocally() throws Exception {
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers("18771803413")
                .setSignName("阿里云短信测试")
                .setTemplateCode("SMS_154950909")
                .setTemplateParam("{\"code\":\"3413\"}");

        SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
        System.out.println(sendSmsResponse);
    }

    @Test
    void redisObject2Json() {
        FeeKey feeKey = new FeeKey();
        feeKey.setName("test");
        redisTemplate.opsForValue()
                .set("test:redis:", feeKey);
    }
}
