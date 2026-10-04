package org.example.aispingboot.service;


import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;


@Service
public class UserCacheService {


    @Resource
    private RedisService redisService;

    @Resource
    private ObjectMapper objectMapper;


    private static final String USER_CACHE_PREFIX =  //统一的用户缓存key前缀格式，避免key冲突
            "user:info:";

    private static final String CACHE_NULL_USER =  // 缓存中表示空值的字符串
            "NULL";

    public UserLoginResponseDTO.UserDetailResponseDTO getUser(Long userId){
        return redisService.get(
                USER_CACHE_PREFIX + userId,
                UserLoginResponseDTO.UserDetailResponseDTO.class
        );
    }
    // 保存用户详情到缓存中
    public void saveUser(Long userId, UserLoginResponseDTO.UserDetailResponseDTO user) {

        String key = USER_CACHE_PREFIX + userId;// 构建用户缓存key，格式为"user:userId"
        int expireTime = 30 + RandomUtil.randomInt(30);  //缓存过期时间，30-60分钟过期

        redisService.set(  //
                key,  // 缓存key
                user,  // 缓存值
                expireTime  // 缓存过期时间，30-60分钟过期
        );

    }

    public void deleteUser(Long userId){
        // 构建用户缓存key，格式为"user:userId"
        redisService.delete(USER_CACHE_PREFIX + userId);  // 从缓存中删除用户详情
    }

    /**
     * 缓存空用户，防止缓存穿透
     */
    public void saveNullUser(Long userId){

        redisService.set(
                USER_CACHE_PREFIX + userId,
                CACHE_NULL_USER,
                5
        );

    }

    public boolean isNullUser(Long userId){

        String value =
                redisService.getString(
                        USER_CACHE_PREFIX + userId
                );

        return CACHE_NULL_USER.equals(value);

    }


    public UserLoginResponseDTO.UserDetailResponseDTO convertUser(
            Object value
    ){

        return objectMapper.convertValue(
                value,
                UserLoginResponseDTO.UserDetailResponseDTO.class
        );

    }

    public boolean hasUserCache(Long userId){

        return redisService.hasKey(
                USER_CACHE_PREFIX + userId
        );

    }

}