package com.campustrade.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginDto {
    @NotBlank(message = "请输入账号")
    private String account;
    @NotBlank(message = "请输入密码")
    private String password;
}
