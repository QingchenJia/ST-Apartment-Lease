package com.atguigu.lease.common.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfiguration {
    /**
     * 创建并配置一个RedisTemplate实例，用于处理键值类型为String和Object的缓存操作
     * 该方法通过Spring的依赖注入机制，获取RedisConnectionFactory并进行配置
     *
     * @param redisConnectionFactory Redis连接工厂，用于建立Redis连接
     * @return 配置好的RedisTemplate实例，用于执行Redis操作
     */
    @Bean
    public RedisTemplate<String, Object> stringObjectRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 实例化RedisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 配置键的序列化方式为字符串序列化，因为键通常使用字符串
        redisTemplate.setKeySerializer(RedisSerializer.string());

        // 配置值的序列化方式为Java对象序列化，因为值可能包含复杂对象
        redisTemplate.setValueSerializer(RedisSerializer.java());

        // 返回配置好的RedisTemplate实例
        return redisTemplate;
    }
}
