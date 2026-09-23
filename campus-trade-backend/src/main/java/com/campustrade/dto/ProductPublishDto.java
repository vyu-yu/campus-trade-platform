package com.campustrade.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
@Data
public class ProductPublishDto {
    @NotBlank(message = "??????")
    private String title;
    private String description;
    @NotNull(message = "??????")
    private BigDecimal price;
    private BigDecimal originalPrice;
    @NotNull(message = "??????")
    private Long categoryId;
    private String condition;
    private String images;
}
