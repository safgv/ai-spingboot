package org.example.aispingboot.DTO.response;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class ConsultationSessionResponseDTO {


    // 会话业务ID
    private String sessionId;


    // 会话标题
    private String sessionTitle;


    // 开始时间
    private LocalDateTime startedAt;


    // 消息数量
    private Integer messageCount;


    // 最后一条消息
    private String lastMessage;

}