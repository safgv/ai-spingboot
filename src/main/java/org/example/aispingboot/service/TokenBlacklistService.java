package org.example.aispingboot.service;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {


    private static final String PREFIX =
            "jwt:blacklist:";


    @Resource
    private RedisService redisService;



    public void addBlacklist(
            String token,
            long expireTime
    ){

        redisService.set(
                PREFIX + token,
                "1",
                expireTime
        );

    }



    public boolean isBlacklist(String token){

        return redisService.hasKey(
                PREFIX + token
        );

    }

}