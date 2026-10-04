package org.example.aispingboot.config;

import cn.hutool.core.text.AntPathMatcher;
import org.example.aispingboot.util.JwtAuthticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration  //告诉Spring Boot这是一个配置类,用于配置Spring Security的Web安全功能，启动时会自动加载该类
@EnableWebSecurity  //开启Web安全功能，包括CSRF保护、会话管理、请求授权等
@EnableMethodSecurity  //开启方法级安全功能，允许在方法级别配置权限检查
public class SecurityConfig {
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private  static final String[] PUBLIC_PATHS = {
            "/",
            "/api/test",
            "/api/user/login",
            "/api/user/add",
    };  //定义的公开路径，无需登录即可访问,用户还没登录也可以访问

    public static Boolean isPublicPATH(String requestUri) {
        for (String publicPath : PUBLIC_PATHS) {  //遍历公开路径数组
            if (antPathMatcher.match(publicPath, requestUri)) {   //判断请求URI是否匹配公开路径
                return true;  //不用认证即可访问
            }
        }
        return false;
    }

    @Bean  //将JwtAuthticationFilter类注册为Spring Bean,用于在Spring Security中使用，交给Spring管理生命周期
    public JwtAuthticationFilter jwtAuthticationFilter() {
        return new JwtAuthticationFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {  //HttpSecurity http配置spring security的规则
        http
                // 禁用CSRF保护 （API服务通常不需要）cookie方式需要禁用CSRF保护，否则会报错
                .csrf(AbstractHttpConfigurer::disable)
                // 配置会话管理为无状态（JWT需要）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        // 公开的路径，无需登录即可访问
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // 其他请求都需要认证
                        .anyRequest().authenticated()
                )
                // 添加JWT认证过滤器，把JwtAuthticationFilter类添加到Spring Security的过滤链中，用于在请求处理前进行JWT认证检查
                .addFilterBefore(jwtAuthticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
