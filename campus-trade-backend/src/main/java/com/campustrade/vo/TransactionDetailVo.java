package com.campustrade.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDetailVo {
    private Long id;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Product info
    private String productTitle;
    private String productDescription;
    private BigDecimal productPrice;
    private BigDecimal productOriginalPrice;
    private String productCondition;
    private String productImages;
    private String productCategoryName;

    // Seller info
    private String sellerNickname;
    private String sellerPhone;
    private String sellerAvatar;

    // Buyer info (current user's role)
    private String myRole;

    // Rating info
    private Long myRatingId;
    private Integer myRating;
    private String myRatingContent;
    private Double sellerAvgRating;
    private Integer sellerRatingCount;
}
