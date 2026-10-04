package org.example.aispingboot.AiService;

public class StructOutPut {
    public record StreamChatSession(
            String sessionId,
            Long userHash,
            String initialMessage,
            Long startTime,
            Long expiryTime,
            Integer messageCount,
            String status
    ) {} //创建会话后的返回DTO，包含会话ID、用户ID、初始消息、开始时间、过期时间、消息数量、状态等
}
