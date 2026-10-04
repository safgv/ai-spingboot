package org.example.aispingboot.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String,Object> redisTemplate(  //RedisTemplate对象
            RedisConnectionFactory factory  //spring自动注入RedisConnectionFactory对象
    ){

        RedisTemplate<String,Object> template =  //RedisTemplate对象赋值给template
                new RedisTemplate<>();  //构建RedisTemplate对象，RedisTemplate是Redis的模板类，用于执行Redis操作


        template.setConnectionFactory(factory);  //设置Redis连接工厂，用于连接Redis数据库

        // 设置key序列化器，用于将key转换为字节数组
        template.setKeySerializer(
                new StringRedisSerializer()
        );

        // 创建ObjectMapper对象，用于将Java对象转换为JSON字符串
        ObjectMapper objectMapper =
                new ObjectMapper();

        // 支持LocalDate、LocalDateTime
        objectMapper.registerModule(
                new JavaTimeModule()
        );



        // value JSON序列化
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(
                        objectMapper
                );

        // 设置value序列化器，用于将value转换为字节数组

        template.setValueSerializer(serializer);
        template.afterPropertiesSet();  //初始化RedisTemplate对象，设置序列化器等属性


        return template;  //返回RedisTemplate对象
           }

}