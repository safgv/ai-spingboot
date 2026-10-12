package org.example.aispingboot.DTO.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


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

    /**
     * 会话详情
     */
    @Data
    public static class SessionDetailResponseDTO {


        // 会话业务ID
        private String sessionId;

        // 会话标题
        private String sessionTitle;

        // 开始时间
        private LocalDateTime startedAt;

        /**
         * 最近情绪分析总结
         */
        private String lastEmotionAnalysis;


        /**
         * 最近情绪更新时间
         */
        private LocalDateTime lastEmotionUpdatedAt;


        /**
         * 聊天消息
         */
        private List<ConsultationMessageResponseDTO> messages;

    }


}