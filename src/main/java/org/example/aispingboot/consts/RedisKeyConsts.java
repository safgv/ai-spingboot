package org.example.aispingboot.consts;


/**
 * Redis Key 常量
 */
public class RedisKeyConsts {


    /**
     * 用户缓存
     */
    public static final String USER_INFO =
            "user:info:";


    /**
     * AI聊天记忆
     */
    public static final String CHAT_MEMORY =
            "chat:memory:";


    /**
     * JWT黑名单
     */
    public static final String JWT_BLACKLIST =
            "jwt:blacklist:";


    private RedisKeyConsts(){

    }

}