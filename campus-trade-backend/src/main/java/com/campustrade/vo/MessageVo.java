package com.campustrade.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVo {
    private Long id;
    private Long userId;
    private Long productId;
    private String content;
    private Long parentId;
    private LocalDateTime createTime;
    private String userNickname;
    private String userAvatar;
    private boolean isSeller;
}
