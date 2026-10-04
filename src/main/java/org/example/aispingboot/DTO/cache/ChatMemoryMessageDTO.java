package org.example.aispingboot.DTO.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemoryMessageDTO {

    // 消息角色：
    // USER / ASSISTANT / SYSTEM
    private String type;

    // 消息文本
    private String content;
}