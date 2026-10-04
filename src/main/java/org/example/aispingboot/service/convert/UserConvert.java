package org.example.aispingboot.service.convert;

import org.example.aispingboot.DTO.command.UserRegisterCommandDTO;
import org.example.aispingboot.DTO.command.UserUpdateCommandDTO;
import org.example.aispingboot.DTO.response.UserLoginResponseDTO;
import org.example.aispingboot.entity.User;
import org.example.aispingboot.enumClass.UserStatus;

import java.time.LocalDateTime;

public class UserConvert {
    // 构建响应DTO中的userInfo
    public static UserLoginResponseDTO.UserDetailResponseDTO entityToDetailResponse(User user) {
        return UserLoginResponseDTO.UserDetailResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())  //昵称
                .avatar(user.getAvatar())    //头像
                .phone(user.getPhone())
                .gender(user.getGender())
                .genderDisplayName(getGenderDisplayName(user.getGender()))  //性别显示名称
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .displayName(user.getDisplayName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static UserLoginResponseDTO entityToLoginResponse(String token, UserLoginResponseDTO.UserDetailResponseDTO userInfo) {
        return UserLoginResponseDTO.builder()  //只是创建 Builder模式的对象，还没有构建完成对象
                .userInfo(userInfo)
                .token(token)
                .roleType(userInfo.getUserType().toString())  //把userType的int类型转换为字符串类型，因为DTO中是字符串类型，方便前端解析
                .build();    // 构建完成对象
    }

    public static User registerCommandToEntity(UserRegisterCommandDTO commandDTO, String encodedPassword) {
        return User.builder()
                .username(commandDTO.getUsername())
                .email(commandDTO.getEmail())
                .password(encodedPassword)
                .nickname(commandDTO.getNickname())
                .phone(commandDTO.getPhone())
                .gender(commandDTO.getGender())
                .birthday(commandDTO.getBirthday())
                .userType(commandDTO.getUserType())
                .status(UserStatus.NORMAL.getCode())  //默认状态为正常
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();  //构建用户实体类User
    }

    /**
     * 获取性别显示名称
     * @param gender 性别代码
     * @return 性别显示名称
     */
    private static String getGenderDisplayName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }

    public static void updateCommandToEntity(
            UserUpdateCommandDTO commandDTO,
            User user
    ){

        user.setNickname(
                commandDTO.getNickname()
        );

        user.setAvatar(
                commandDTO.getAvatar()
        );

        user.setPhone(
                commandDTO.getPhone()
        );

        user.setEmail(
                commandDTO.getEmail()
        );

        user.setGender(
                commandDTO.getGender()
        );

        user.setBirthday(
                commandDTO.getBirthday()
        );

    }

}
