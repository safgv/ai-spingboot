package org.example.aispingboot.service;
import jakarta.annotation.Resource;
import org.example.aispingboot.consts.RedisKeyConsts;
import org.example.aispingboot.util.TokenHashUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    @Resource
    private RedisService redisService;



    public void addBlacklist(
            String token,
            long expireTime
    ){

        redisService.set(
                RedisKeyConsts.JWT_BLACKLIST + TokenHashUtil.hash(token),
                "1",
                expireTime
        );

    }



    public boolean isBlacklist(String token){

        return redisService.hasKey(
                RedisKeyConsts.JWT_BLACKLIST + TokenHashUtil.hash(token)
        );

    }

}