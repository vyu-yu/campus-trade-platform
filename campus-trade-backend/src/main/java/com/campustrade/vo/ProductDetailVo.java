package com.campustrade.vo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProductDetailVo {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String condition;
    private String images;
    private String status;
    private Integer viewCount;
    private String categoryName;
    private Long categoryId;
    private UserInfoVo seller;
    private String buyerNickname;
    private String buyerPhone;
    private boolean favorited;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
