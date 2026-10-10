package org.example.aispingboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.aispingboot.DTO.command.UserLoginCommandDTO;
import org.example.aispingboot.DTO.command.UserRegisterCommandDTO;
import org.example.aispingboot.DTO.command.UserUpdateCommandDTO;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.service.TokenBlacklistService;
import org.example.aispingboot.service.UserService;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    // 用户登录接口
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commandDTO) {


        // 调用服务层登录方法
        UserLoginResponseDTO result = userService.login(commandDTO);  //根据登录命令DTO，返回登录响应DTO，赋值给result变量
        return Result.ok(result);  //返回登录响应DTO
    }

    // 用户注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandDTO) {
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commandDTO);
        return Result.ok(result);   //注册成功返回用户详情响应DTO即可，不包括token
    }

    // 获取当前用户
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        // 如何从token中解析出用户的id
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        // 调用service层获取用户详情
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.ok(result);
    }

    // 更新用户信息接口
    @PutMapping("/update")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> updateUser(@Valid @RequestBody UserUpdateCommandDTO commandDTO) {
        String token = JwtTokenUtil.getCurrentToken();  // 当前请求中的token

        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);  // 验证token，返回解码后的JWT对象

        Long userId = jwt.getClaim("userId").asLong();  // 从解码后的JWT对象中获取用户id

        commandDTO.setId(userId);  // 将用户id赋值给更新命令DTO的id字段

        UserLoginResponseDTO.UserDetailResponseDTO result = userService.updateUser(commandDTO);
        return Result.ok(result);
    }

    // 用户退出登录接口

    @PostMapping("/logout")
    public Result<?> logout() {

        // 从token中解析出用户的id
        String token = JwtTokenUtil.getCurrentToken();
        if(token != null) {
            // 获取token过期时间
            // 1440分钟
            long expireMinutes = JwtTokenUtil.getExpireMinutes(token);
            if(expireMinutes > 0) {
                tokenBlacklistService.addBlacklist(token, expireMinutes);
            }
        }
        // 清除security context
        SecurityContextHolder.clearContext();

        log.info("退出登录成功");

        // 调用服务层退出登录方法
        return Result.ok();
    }



}
