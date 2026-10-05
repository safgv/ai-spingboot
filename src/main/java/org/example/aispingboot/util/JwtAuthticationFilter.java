package org.example.aispingboot.util;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.config.SecurityConfig;
import org.example.aispingboot.enumClass.UserStatus;
import org.example.aispingboot.service.TokenBlacklistService;
import org.example.aispingboot.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


public class JwtAuthticationFilter extends OncePerRequestFilter {  //请求过滤器，用于在请求处理前进行JWT认证检查是否需要认证，OncePerRequestFilter表示每一个HTTP请求只执行一次
    @Resource
    private UserService userService;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // 检查是否为公开路径
        System.out.println(
                "shouldNotFilter:"
                        + request.getRequestURI()
        );
        return SecurityConfig.isPublicPATH(requestUri);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // 获取请求的URI和方法
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        System.out.println(requestUri);
        System.out.println(method);
        //Jwt认证流程八个步骤
        // 1. 提取 JWT token
        String token = JwtTokenUtil.extractTokenFromRequest(request);
        System.out.println(
                "token=" + token
        );
        if (StringUtils.hasText(token)) {

            // 2. 检查token是否在黑名单中
            if(tokenBlacklistService.isBlacklist(token)) {
                clearSecurityContext();
                ResponseUtil.writeError(
                        response,
                        ResultCode.TOKEN_INVALID
                );
                return;
            }

            // 3. 验证token并获取用户信息
            JwtTokenUtil.TokenVerificationResult validationResult = JwtTokenUtil.validateToken(token);  //验证token是否有效，返回验证结果，是封装后的类，包含了userId、username、roleType等信息
            System.out.println(
                    "验证结果=" + validationResult
            );
            if (validationResult != null && validationResult.isValid()) {
                // 3. 根据userId查询数据库中的用户信息
                UserLoginResponseDTO.UserDetailResponseDTO user = userService.getUserById(validationResult.getUserId()); //根据userId查询数据库中的用户信息
                System.out.println(JSONUtil.parseObj(user));
                if (user != null && UserStatus.NORMAL.getCode().equals(user.getStatus())) {
                    // 4. 创建Spring Security认证对象
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(  //创建SimpleGrantedAuthority对象，用于Spring Security权限校验
                            new SimpleGrantedAuthority("ROLE_" + validationResult.getRoleType())  //SimpleGrantedAuthority对象，包含了用户和用户的权限，用于Spring Security权限校验
                    );

                    // 创建UsernamePasswordAuthenticationToken对象
                     UsernamePasswordAuthenticationToken authcation = new UsernamePasswordAuthenticationToken(  //Spring Security 的一个认证对象，表示当前用户已经登录，包含了用户的身份和权限
                            validationResult.getUsername(), // 用户名作为主体
                            null,
                            authorities
                    );  //告诉Spring Security当前用户的身份和权限

                     // 设置认证信息到Spring Securtity上下文，保存当前用户的身份和权限，后续请求Controller可以使用SecurityContextHolder获取到当前用户的身份和权限
                    SecurityContextHolder.getContext().setAuthentication(authcation);

                    // 将token存储到请求属性中,后续请求Controller可以使用request.getAttribute("jwtToken")获取到当前token
                    request.setAttribute("jwtToken", token);
                } else {
                    clearSecurityContext();
                    ResponseUtil.writeError(response, ResultCode.TOKEN_ACCESS_FORBIDDEN);
                }
            } else {
                clearSecurityContext();
                ResponseUtil.writeError(response, ResultCode.TOKEN_INVALID);
            }
        } else {
            // 清理上下文
            clearSecurityContext();
            ResponseUtil.writeError(response, ResultCode.ACCESS_UNAUTHORIZED);
            return;
        }
        // 继续过滤器链
        chain.doFilter(request, response);  // 继续执行后续的过滤器，直到到达Controller
    }

    // 清理Spring Security上下文
    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
