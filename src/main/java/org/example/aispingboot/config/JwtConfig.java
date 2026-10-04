package org.example.aispingboot.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Spring读取配置文件application.properties中的JWT相关参数，保存到JwtConfig类中
@Data
@Component   //标记一个类为 Spring Bean，Spring 启动时会扫描到它，自动创建对象并放进容器
@ConfigurationProperties(prefix="jwt")  // 配置类的前缀，用于在application.properties中配置JWT相关参数
public class JwtConfig {
    private String secret;  //JWT签名密钥
    private long expiration;  //JWT过期时间（毫秒）
    private long refreshExpiration;   //JWT刷新过期时间（毫秒）
    private String header;  //JWT请求头名称
    private String tokenPrefix;  //JWT token 前缀
}

// 属于config包下的配置类，用于配置JWT相关参数
