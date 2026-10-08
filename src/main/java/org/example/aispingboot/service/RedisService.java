package org.example.aispingboot.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class RedisService {


    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @Resource
    private ObjectMapper objectMapper;

    //泛型方法，泛型集合对象获取
    public <T> T get(
            String key,
            TypeReference<T> typeReference
    ){

        Object value =
                redisTemplate.opsForValue()
                        .get(key);

        if(value == null){
            return null;
        }

        return objectMapper.convertValue(
                value,
                typeReference
        );
    }


    /**
     * 获取缓存
     *单个对象
     * @param key Redis key
     * @param clazz 返回对象类型
     */
    public <T> T get(
            String key,
            Class<T> clazz
    ){

        Object value =
                redisTemplate.opsForValue()
                        .get(key);


        if(value == null){

            return null;

        }


        return objectMapper.convertValue(
                value,
                clazz
        );

    }



    /**
     * 保存缓存
     */
    public void set(
            String key,
            Object value,
            long timeout
    ){

        redisTemplate.opsForValue()
                .set(
                        key,
                        value,
                        timeout,
                        TimeUnit.MINUTES
                );

    }



    /**
     * 删除缓存
     */
    public void delete(
            String key
    ){

        redisTemplate.delete(key);

    }

    public boolean hasKey(String key){

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));

    }

    public String getString(String key){

        Object value =
                redisTemplate.opsForValue()
                        .get(key);

        return value == null ?
                null :
                value.toString();

    }

    public void leftPush(
            String key,
            Object value
    ){

        redisTemplate.opsForList()
                .leftPush(
                        key,
                        value
                );

    }

    public List<Object> getList(
            String key
    ){

        return redisTemplate.opsForList()
                .range(
                        key,
                        0,
                        -1
                );

    }

}