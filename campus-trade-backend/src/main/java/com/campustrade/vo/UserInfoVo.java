package com.campustrade.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserInfoVo {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private LocalDateTime createTime;
}
