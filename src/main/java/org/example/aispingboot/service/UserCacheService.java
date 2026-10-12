package org.example.aispingboot.service;


import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.consts.RedisKeyConsts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;


@Service
public class UserCacheService {


    @Resource
    private RedisService redisService;

    private static final String CACHE_NULL_USER = "NULL"; // 缓存中表示空值的字符串

    private static final int USER_CACHE_MINUTES = 30;

    private static final int USER_CACHE_RANDOM = 30;

    public UserLoginResponseDTO.UserDetailResponseDTO getUser(Long userId){
        String key = RedisKeyConsts.USER_INFO + userId;


        if(isNullUser(userId)){

            return null;

        }

        return redisService.get(
                key,
                UserLoginResponseDTO.UserDetailResponseDTO.class
        );
    }
    // 保存用户详情到缓存中
    public void saveUser(Long userId, UserLoginResponseDTO.UserDetailResponseDTO user) {

        String key = RedisKeyConsts.USER_INFO + userId;// 构建用户缓存key，格式为"user:userId"
        int expireTime = USER_CACHE_MINUTES + RandomUtil.randomInt(USER_CACHE_RANDOM);  //缓存过期时间，30-60分钟过期

        redisService.set(  //
                key,  // 缓存key
                user,  // 缓存值
                expireTime  // 缓存过期时间，30-60分钟过期
        );

    }

    public void deleteUser(Long userId){
        // 构建用户缓存key，格式为"user:userId"
        redisService.delete(RedisKeyConsts.USER_INFO + userId);  // 从缓存中删除用户详情
    }

    /**
     * 缓存空用户，防止缓存穿透
     */
    public void saveNullUser(Long userId){

        redisService.set(
                RedisKeyConsts.USER_INFO + userId,
                CACHE_NULL_USER,
                5
        );

    }

    public boolean isNullUser(Long userId){

        String value =
                redisService.getString(
                        RedisKeyConsts.USER_INFO + userId
                );

        return CACHE_NULL_USER.equals(value);

    }

    public boolean hasUserCache(Long userId){

        return redisService.hasKey(
                RedisKeyConsts.USER_INFO + userId
        );

    }

}