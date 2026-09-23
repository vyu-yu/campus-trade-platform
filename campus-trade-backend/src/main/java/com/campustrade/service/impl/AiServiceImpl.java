package com.campustrade.service.impl;

import com.campustrade.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:gpt-3.5-turbo}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    private Map<String,Object> callAI(String systemPrompt, String userMessage) {
        Map<String,Object> result = new HashMap<>();
        if (apiKey == null || apiKey.isEmpty()) {
            result.put("error", "AI API Key 未配置，请在 application.yml 中设置 ai.api-key");
            return result;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null) {
                Map<String, String> sysMsg = new HashMap<>();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt);
                messages.add(sysMsg);
            }
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map body = response.getBody();
                if (body != null && body.containsKey("choices")) {
                    List<Map> choices = (List<Map>) body.get("choices");
                    if (!choices.isEmpty()) {
                        Map choice = choices.get(0);
                        Map message = (Map) choice.get("message");
                        String content = (String) message.get("content");
                        result.put("content", content.trim());
                        return result;
                    }
                }
            }
            result.put("error", "AI 调用失败: " + response.getStatusCode());
        } catch (Exception e) {
            result.put("error", "AI 调用异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String,Object> generateTitle(String description, String category) {
        Map<String,Object> aiResult = callAI(
            "你是一个校园二手交易平台的标题生成助手。请根据用户提供的商品描述和分类，生成一个简短、吸引人的商品标题。要求：1. 标题不超过20个字 2. 包含商品核心信息 3. 简洁明了 4. 只返回标题内容，不要多余文字",
            "商品描述：" + (description != null ? description : "无") + "\n商品分类：" + (category != null ? category : "未指定")
        );

        Map<String,Object> result = new HashMap<>();
        if (aiResult.containsKey("content")) {
            String title = (String) aiResult.get("content");
             title = title.replaceAll("[\",]", "").trim();
            result.put("title", title.length() > 50 ? title.substring(0, 50) : title);
        } else {
            String title;
            if (description != null && description.length() > 5) {
                title = description.substring(0, Math.min(15, description.length()));
                if (category != null) title = category + title;
            } else {
                title = (category != null ? category : "") + "二手商品";
            }
            result.put("title", title + "-校园二手交易");
        }
        result.put("note", "此标题由AI自动生成，可手动修改");
        return result;
    }

    @Override
    public Map<String,Object> optimizeDescription(String description, String condition) {
        Map<String,Object> aiResult = callAI(
            "你是一个校园二手交易平台的商品描述优化助手。请根据用户提供的原始描述和成色信息，优化商品描述，使其更加详细、吸引人。要求：1. 保留原始关键信息 2. 语言简洁流畅 3. 突出商品卖点 4. 控制在100字以内 5. 只返回优化后的描述内容",
            "原始描述：" + (description != null ? description : "无") + "\n商品成色：" + (condition != null ? condition : "未说明")
        );

        Map<String,Object> result = new HashMap<>();
        if (aiResult.containsKey("content")) {
            String desc = (String) aiResult.get("content");
             desc = desc.replaceAll("[\",]", "").trim();
            result.put("description", desc.length() > 500 ? desc.substring(0, 500) : desc);
        } else {
            StringBuilder sb = new StringBuilder();
            if (description != null) sb.append(description);
            if (condition != null) sb.append("\n\n商品成色：").append(condition);
            sb.append("\n欢迎学校同学们咨询，支持自提和校内交易。");
            result.put("description", sb.toString());
        }
        result.put("note", "此描述由AI优化生成，可手动修改");
        return result;
    }

    @Override
    public Map<String,Object> customerService(String question) {
        Map<String,Object> aiResult = callAI(
            "你是一个校园二手交易平台的智能客服。请耐心、友好地回答用户的问题。保持回答简洁实用。",
            "用户问题：" + (question != null ? question : "你好")
        );

        Map<String,Object> result = new HashMap<>();
        result.put("question", question);
        if (aiResult.containsKey("content")) {
            result.put("answer", aiResult.get("content"));
        } else {
            String q = question != null ? question.toLowerCase() : "";
            if (q.contains("交易") || q.contains("怎么买") || q.contains("怎么卖")) {
                result.put("answer", "交易流程：\n1. 浏览商品列表\n2. 点击商品进入详情页\n3. 点击购买创建交易\n4. 与卖家沟通确认\n5. 完成交易后更新状态");
            } else if (q.contains("举报") || q.contains("投诉")) {
                result.put("answer", "举报流程：\n1. 在商品详情页点击举报按钮\n2. 选择举报原因并提交\n3. 管理员将处理并通知您");
            } else if (q.contains("退款") || q.contains("退货")) {
                result.put("answer", "建议交易前充分沟通。如遇争议可通过举报功能请求平台帮助。");
            } else {
                result.put("answer", "您好！欢迎使用校园二手交易平台AI小助手。请问有什么可以帮助您的？");
            }
        }
        return result;
    }

    private double getConditionRate(String condition) {
        if (condition == null) return 0.7;
        return switch (condition) {
            case "全新" -> 1.0;
            case "几乎全新" -> 0.85;
            case "轻微使用痕迹" -> 0.7;
            case "明显使用痕迹" -> 0.5;
            default -> 0.3;
        };
    }

    @Override
    public Map<String,Object> priceSuggestion(String category, String condition, Double originalPrice) {
        if (originalPrice == null || originalPrice <= 0) {
            Map<String,Object> result = new HashMap<>();
            result.put("suggestedPrice", 0);
            result.put("priceRange", "请填写原价");
            result.put("condition", condition);
            result.put("referenceNote", "请先输入原价");
            return result;
        }
        double rate = getConditionRate(condition);
        double categoryFactor = 1.0;
        if (category != null) {
            if (category.contains("电子") || category.contains("数码")) categoryFactor = 0.9;
            else if (category.contains("教材") || category.contains("书籍")) categoryFactor = 0.7;
            else if (category.contains("服饰") || category.contains("鞋包")) categoryFactor = 1.0;
            else if (category.contains("体育") || category.contains("运动")) categoryFactor = 0.85;
            else if (category.contains("生活")) categoryFactor = 0.8;
        }
        double priceFactor = 1.0;
        if (originalPrice > 5000) priceFactor = 0.85;
        else if (originalPrice > 2000) priceFactor = 0.9;
        else if (originalPrice > 500) priceFactor = 0.95;
        double suggestedPrice = originalPrice * rate * categoryFactor * priceFactor;
        double minPrice = suggestedPrice * 0.85;
        double maxPrice = suggestedPrice * 1.15;
        double floor = originalPrice * 0.1;
        if (suggestedPrice < floor) {
            suggestedPrice = floor;
            minPrice = floor * 0.9;
            maxPrice = floor * 1.2;
        }
        Map<String,Object> result = new HashMap<>();
        result.put("suggestedPrice", Math.round(suggestedPrice * 100) / 100.0);
        result.put("priceRange", String.format("%.2f - %.2f", minPrice, maxPrice));
        result.put("condition", condition);
        result.put("referenceNote", String.format("基于原价%.0f、%s、%s综合计算", originalPrice, condition, category != null ? category : "其他"));
        return result;
    }

    @Override
    public Map<String,Object> riskCheck(String title, String description, Double price) {
        List<String> warnings = new ArrayList<>();
        int riskLevel = 0;
        String text = (title != null ? title : "") + " " + (description != null ? description : "");
        String[] suspiciousKeywords = {"假货", "代购", "发票", "全新未拆", "正品代购", "支付宝转账"};
        for (String kw : suspiciousKeywords) {
            if (text.contains(kw)) {
                warnings.add("检测到可疑关键词\"" + kw + "\"");
                riskLevel++;
            }
        }
        if (price != null && price < 5) { warnings.add("价格异常低廉"); riskLevel++; }
        if (price != null && price > 10000) { warnings.add("价格较高"); riskLevel++; }
        if (text.length() < 10) warnings.add("描述过短");
        Map<String,Object> result = new HashMap<>();
        result.put("riskLevel", riskLevel == 0 ? "低风险" : (riskLevel <= 2 ? "中等风险" : "高风险"));
        result.put("warnings", warnings);
        result.put("suggestion", riskLevel > 0 ? "建议谨慎处理" : "未检测到明显风险");
        return result;
    }
}
