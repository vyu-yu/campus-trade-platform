package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired private AiService aiService;
    @PostMapping("/generate-title")
    public Result<Map<String,Object>> generateTitle(@RequestParam String description,
            @RequestParam(required = false) String category) {
        return Result.success(aiService.generateTitle(description, category));
    }
    @PostMapping("/optimize-description")
    public Result<Map<String,Object>> optimizeDescription(@RequestParam String description,
            @RequestParam(required = false) String condition) {
        return Result.success(aiService.optimizeDescription(description, condition));
    }
    @PostMapping("/price-suggestion")
    public Result<Map<String,Object>> priceSuggestion(@RequestParam(required = false) String category,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Double originalPrice) {
        return Result.success(aiService.priceSuggestion(category, condition, originalPrice));
    }
    @PostMapping("/customer-service")
    public Result<Map<String,Object>> customerService(@RequestParam String question) {
        return Result.success(aiService.customerService(question));
    }
    @PostMapping("/risk-check")
    public Result<Map<String,Object>> riskCheck(@RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double price) {
        return Result.success(aiService.riskCheck(title, description, price));
    }
}
