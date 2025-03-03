package com.atguigu.lease.common.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SmsProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
public class SmsClientConfiguration {
    @Resource
    private SmsProperties properties;

    /**
     * 创建并初始化Client对象的Bean
     * 该方法使用了@ConfigurationProperties和@Bean注解，使其支持从配置属性中自动装载Client的配置信息
     * 并通过这些配置信息创建一个Client对象
     *
     * @return Client对象，根据配置属性初始化
     * @throws RuntimeException 如果Client对象创建失败，则抛出运行时异常
     */
    @Bean
    public Client createClient() {
        // 创建Config对象，用于设置Client的配置信息
        Config config = new Config();

        // 设置Client的访问端点
        config.setEndpoint(properties.getEndpoint());
        // 设置Client的访问密钥ID
        config.setAccessKeyId(properties.getAccessKeyId());
        // 设置Client的访问密钥密钥
        config.setAccessKeySecret(properties.getAccessKeySecret());

        try {
            // 使用配置信息创建Client对象
            return new Client(config);
        } catch (Exception e) {
            // 如果创建Client对象时发生异常，则打印异常信息并抛出运行时异常
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
