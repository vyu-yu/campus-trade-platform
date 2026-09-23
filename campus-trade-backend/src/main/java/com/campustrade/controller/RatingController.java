package com.campustrade.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.Result;
import com.campustrade.entity.Rating;
import com.campustrade.mapper.RatingMapper;
import com.campustrade.mapper.TransactionMapper;
import com.campustrade.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api/rating")
public class RatingController {
    @Autowired private RatingMapper ratingMapper;
    @Autowired private TransactionMapper transactionMapper;

    @PostMapping
    public Result<?> create(Authentication auth, @RequestBody Rating rating) {
        Long userId = (Long) auth.getPrincipal();
        rating.setId(null);
        rating.setUserId(userId);
        Rating exist = ratingMapper.selectOne(new LambdaQueryWrapper<Rating>().eq(Rating::getUserId, userId).eq(Rating::getTransactionId, rating.getTransactionId()));
        if (exist != null) return Result.error(400, "已评价过此交易");
        ratingMapper.insert(rating);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(Authentication auth, @PathVariable Long id, @RequestBody Rating rating) {
        Long userId = (Long) auth.getPrincipal();
        Rating exist = ratingMapper.selectById(id);
        if (exist == null) return Result.error(404, "?????");
        if (!exist.getUserId().equals(userId)) return Result.error(403, "????????");
        exist.setScore(rating.getScore());
        if (rating.getContent() != null) exist.setContent(rating.getContent());
        ratingMapper.updateById(exist);
        return Result.success();
    }

    @GetMapping("/seller/{sellerId}")
    public Result<Map<String, Object>> sellerStats(@PathVariable Long sellerId) {
        List<Rating> list = ratingMapper.selectList(new LambdaQueryWrapper<Rating>().eq(Rating::getSellerId, sellerId));
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        double avg = list.stream().mapToInt(Rating::getScore).average().orElse(0);
        data.put("avgScore", Math.round(avg * 10) / 10.0);
        // Per-product ratings
        Map<Long, List<Rating>> byProduct = list.stream().collect(java.util.stream.Collectors.groupingBy(Rating::getTransactionId));
        List<Map<String, Object>> detailRatings = new ArrayList<>();
        for (Map.Entry<Long, List<Rating>> entry : byProduct.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("transactionId", entry.getKey());
            Rating firstRating = entry.getValue().get(0);
            Transaction tx = transactionMapper.selectById(firstRating.getTransactionId());
            item.put("productId", tx != null ? tx.getProductId() : null);
            item.put("content", firstRating.getContent());
            double prodAvg = entry.getValue().stream().mapToInt(Rating::getScore).average().orElse(0);
            item.put("score", Math.round(prodAvg * 10) / 10.0);
            item.put("count", entry.getValue().size());
            detailRatings.add(item);
        }
        data.put("detailRatings", detailRatings);
        return Result.success(data);
    }
}