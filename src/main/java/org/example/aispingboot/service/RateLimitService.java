package org.example.aispingboot.service;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aispingboot.consts.RedisKeyConsts;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitService {

    @Resource
    private RedisService redisService; // 注入RedisService组件，用于调用RedisService的方法操作Redis

    private static final int MAX_REQUEST_COUNT = 10;  // 每分钟最多10次访问

    /**
     * AI聊天限流
     *
     * @return true 允许访问
     *         false 超过限制
     */

    public boolean allowAIChat(Long userId){




        String key = RedisKeyConsts.AI_CHAT_RATE_LIMIT + userId;  // 构建Redis key，用于存储用户访问次数

               Long count = redisService.increment(key);  // 自增用户访问次数

        log.debug(
                "AI限流检查,userId={},count={}",
                userId,
                count
        );

        // 第一次访问设置一分钟过期
        if(count == 1){

            redisService.expire(
                    key,
                    1,
                    TimeUnit.MINUTES
            );

        }




        return count <= MAX_REQUEST_COUNT;  // 如果访问次数不超过最大限制，返回true，否则返回false

    }

}