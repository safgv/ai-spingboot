package org.example.aispingboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
// 实体类：创建咨询话话的请求参数
@Data
public class ConsultationSessionCreateDTO {
    @Size(max = 200, message = "会话标题最多200个字符")
    private String sessionTitle;

    @NotBlank(message = "初始消息不能为空")
    @Size(max = 2000, message = "初始消息最多2000个字符")
    private String initialMessage;
}
