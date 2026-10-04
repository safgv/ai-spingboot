package org.example.aispingboot;


import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UserServiceTest {


    @Autowired
    private UserService userService;


    @Test
    public void testUserCache(){


        // 第一次查询
        UserLoginResponseDTO.UserDetailResponseDTO user1 =
                userService.getUserById(2L);


        System.out.println(
                "第一次查询结果："
                        + user1
        );



        // 第二次查询
        UserLoginResponseDTO.UserDetailResponseDTO user2 =
                userService.getUserById(2L);


        System.out.println(
                "第二次查询结果："
                        + user2
        );

    }

}