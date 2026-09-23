package com.campustrade.dto;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class ProductSearchDto {
    private String keyword;
    private Long categoryId;
    private String condition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;
    private String sortOrder;
    private Long userId;
    private Integer page = 1;
    private Integer pageSize = 12;
}
