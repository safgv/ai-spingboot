package org.example.aispingboot;


import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;


@SpringBootTest
public class RedisTest {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Test
    public void testRedisDTO() {


        // 1. 使用Builder创建DTO对象
        UserLoginResponseDTO.UserDetailResponseDTO user =
                UserLoginResponseDTO.UserDetailResponseDTO
                        .builder()
                        .id(100L)
                        .username("admin")
                        .email("admin@test.com")
                        .nickname("管理员")
                        .avatar("avatar.png")
                        .phone("13800000000")
                        .gender(1)
                        .genderDisplayName("男")
                        .userType(1)
                        .userTypeDisplayName("普通用户")
                        .status(1)
                        .statusDisplayName("正常")
                        .displayName("管理员")
                        .build();


        // 2. 保存DTO对象到Redis
        redisTemplate.opsForValue()
                .set(
                        "user:100",
                        user
                );


        // 3. 从Redis读取对象
        Object value =
                redisTemplate.opsForValue()
                        .get("user:100");


        // 4. 输出结果
        System.out.println(
                "Redis读取对象：" + value
        );


        // 5. 查看对象类型
        System.out.println(
                "对象类型：" + value.getClass()
        );

    }

}