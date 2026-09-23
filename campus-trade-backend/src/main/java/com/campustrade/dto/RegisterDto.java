package com.campustrade.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class RegisterDto {
    @NotBlank(message = "请输入手机号")
    private String phone;
    @NotBlank(message = "请输入密码")
    private String password;
    private String nickname;
    private String email;
}
