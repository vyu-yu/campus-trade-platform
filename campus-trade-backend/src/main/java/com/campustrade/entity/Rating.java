package com.campustrade.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@TableName("rating")
public class Rating {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sellerId;
    private Long transactionId;
    private Integer score;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}