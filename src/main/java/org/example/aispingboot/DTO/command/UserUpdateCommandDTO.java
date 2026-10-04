package org.example.aispingboot.DTO.command;


import lombok.Data;

import java.time.LocalDate;


@Data
public class UserUpdateCommandDTO {


    private Long id;


    private String nickname;


    private String avatar;


    private String phone;


    private String email;


    private Integer gender;


    private LocalDate birthday;

}