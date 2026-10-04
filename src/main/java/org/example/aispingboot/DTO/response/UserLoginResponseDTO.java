package org.example.aispingboot.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UserLoginResponseDTO {
    private String token;
    private String roleType;
    private UserDetailResponseDTO userInfo;
    @Builder   // 用于构建UserDetailResponseDTO对象,直接通过Builder模式创建，避免使用new关键字
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetailResponseDTO {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer gender;
        private String genderDisplayName;
        private LocalDate birthday;
        private Integer userType;
        private String userTypeDisplayName;
        private Integer status;
        private String statusDisplayName;
        private String displayName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
