package com.campustrade.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
@Data
public class ReportSubmitDto {
    @NotNull(message = "??ID????")
    private Long productId;
    @NotBlank(message = "????????")
    private String reason;
    private String description;
}
