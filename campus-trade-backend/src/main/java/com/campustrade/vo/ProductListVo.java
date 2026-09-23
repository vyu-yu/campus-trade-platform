package com.campustrade.vo;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class ProductListVo {
    private Long id;
    private String title;
    private BigDecimal price;
    private String condition;
    private String images;
    private String status;
    private Integer viewCount;
    private String categoryName;
    private String sellerNickname;
    private String buyerNickname;
    private String buyerPhone;
    private LocalDateTime createTime;
}
