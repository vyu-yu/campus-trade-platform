package com.campustrade.service;
import java.util.Map;
public interface AiService {
    Map<String,Object> generateTitle(String description, String category);
    Map<String,Object> optimizeDescription(String description, String conditionAlias);
    Map<String,Object> priceSuggestion(String category, String conditionAlias, Double originalPrice);
    Map<String,Object> customerService(String question);
    Map<String,Object> riskCheck(String title, String description, Double price);
}
