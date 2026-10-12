package org.example.aispingboot.util;

import ch.qos.logback.core.util.StringUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.example.aispingboot.config.JwtConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
// 属于util包下的工具类，用于生成JWT token
// 生成 token
// 获取 token
// 验证 token
// 解析 token
@Component
public class JwtTokenUtil implements ApplicationContextAware {
    private static final String ISSUER = "mental-health-assistant";

    private static ApplicationContext applicationContext;
    // 用于在静态工具类中获取Spring容器管理的Bean
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        JwtTokenUtil.applicationContext = applicationContext;  // 保存Spring容器
    }


    // 把Spring容器保存起来，方便在静态工具类中获取Bean
    private static JwtConfig getJwtConfig() {
        return applicationContext.getBean(JwtConfig.class);
    };// 从Spring容器中获取JwtConfig Bean，返回JwtConfig对象

    // 生成token的方法
    public static String generateToken(Long userId, String username, Integer roleType) {
        try {
            // 获取jwt的配置
            JwtConfig jwtConfig = getJwtConfig();//getJwtConfig()方法得到JwtConfig对象，赋值给jwtConfig
            // 生成签名的算法
            Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());
            // 生成过期时间
            Date expiration = new Date(System.currentTimeMillis() + jwtConfig.getExpiration());

            String token = JWT.create()//创建JWT对象，用于生成token，把对象赋值给token变量
                    .withClaim("userId", userId)   // 添加用户ID，从用户登录信息中获取的
                    .withClaim("username", username)  // 添加用户名，怎么获取的
                    .withClaim("roleType", roleType)  // 添加角色类型
                    .withExpiresAt(expiration) // 设置过期时间
                    .withIssuedAt(new Date()) // 设置签发时间
                    .withIssuer(ISSUER) // 设置签发者
                    .sign(algorithm);
            return token;
        } catch (Exception e) {
            throw new RuntimeException("生成token 失败: " + e);
        }
    }

    // 提取token
    public static String extractTokenFromRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String tokenHeader = request.getHeader("Authorization");  //标准的Authorization: Bearer xxx
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {  // 如果token头存在且不为空，且以Bearer "开头
            return tokenHeader.substring(7);
        }
        return null;
    }

    // 获取当前token
    public static String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                return token;
            }

            // 备用方案： 从请求头直接获取
            String headerToken =  extractTokenFromRequest(request);
            return headerToken;
        }
        return null;
    }

    // 验证token
    public static TokenVerificationResult validateToken(String token) {
        DecodedJWT jwt = verifyToken(token);  // 验证token是否有效，返回解码后的JWT对象,解析token中的信息
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();

        // 角色类型
        Integer roleType = null;
        try {
            roleType = jwt.getClaim("roleType").asInt();
        } catch (Exception e) {
           String roleTypeStr = jwt.getClaim("roleType").asString();
           if (StringUtils.hasText(roleTypeStr)) {
               roleType = Integer.valueOf(roleTypeStr);  // 将字符串转换为整数
           }
        }
        if (userId != null && StringUtils.hasText(username) && roleType != null) {
            return new TokenVerificationResult(userId, username, roleType, true);  //封装验证结果，因为后面过滤器需要使用到userId、username、roleType等信息
        }
        return null;
    }
    //为了得到有意义的结果，需要对token进行解码，提取出userId、username、roleType等信息

    // 验证token有效性
    public static DecodedJWT verifyToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new JWTVerificationException("Token不能为空");
        }
        // token解码
        JwtConfig jwtConfig = getJwtConfig();  // 从Spring容器中获取JWT配置
        Algorithm algorithm = Algorithm.HMAC256(jwtConfig.getSecret());  //使用之前生成token的算法和密钥进行验证
        JWTVerifier verifier =  JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();  // 构建JWT验证器，设置算法和签发者
        return  verifier.verify(token);  // 验证token是否有效，返回解码后的JWT对象
    }

    // Token验证结果封装类
    @Getter
    public static class TokenVerificationResult {
        private final Long userId;
        private final String username;
        private final Integer roleType;
        private final boolean valid;

        public TokenVerificationResult(Long userId, String username, Integer roleType, boolean valid) {
            this.userId = userId;
            this.username = username;
            this.roleType = roleType;
            this.valid = valid;
        }
    }

    public static long getExpireMinutes(String token){

        DecodedJWT jwt = verifyToken(token);

        Date expiresAt = jwt.getExpiresAt();

        long millis =
                expiresAt.getTime()
                        - System.currentTimeMillis();


        return millis / 1000 / 60;

    }
}
