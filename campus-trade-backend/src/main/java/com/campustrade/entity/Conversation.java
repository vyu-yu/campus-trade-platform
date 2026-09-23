package com.campustrade.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer buyerUnread;
    private Integer sellerUnread;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}