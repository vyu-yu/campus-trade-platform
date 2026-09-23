package com.campustrade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.common.Result;
import com.campustrade.entity.*;
import com.campustrade.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserMapper userMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private TransactionMapper transactionMapper;
    @Autowired private ReportMapper reportMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    // ===== 仪表盘 =====
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("productCount", productMapper.selectCount(null));
        data.put("transactionCount", transactionMapper.selectCount(null));
        data.put("reportCount", reportMapper.selectCount(null));
        data.put("pendingReportCount", reportMapper.selectCount(
            new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING")));
       data.put("sellingCount", productMapper.selectCount(
           new LambdaQueryWrapper<Product>().eq(Product::getStatus, "SELLING")));
        data.put("soldCount", transactionMapper.selectCount(
            new LambdaQueryWrapper<Transaction>().eq(Transaction::getStatus, "COMPLETED")));

        // Daily stats for charts (last 7 days)
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = today.minusDays(i);
            java.time.LocalDateTime dayStart = day.atStartOfDay();
            java.time.LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", day.toString());
            dayData.put("newUsers", userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreateTime, dayStart, dayEnd)));
            dayData.put("newProducts", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().between(Product::getCreateTime, dayStart, dayEnd)));
            dayData.put("newTransactions", transactionMapper.selectCount(
                new LambdaQueryWrapper<Transaction>().between(Transaction::getCreateTime, dayStart, dayEnd)));
            dailyStats.add(dayData);
        }
        data.put("dailyStats", dailyStats);

        return Result.success(data);
    }

    // ===== 用户管理 =====
    @GetMapping("/users")
    public Result<IPage<User>> users(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int pageSize,
                                      @RequestParam(required = false) String keyword) {
        Page<User> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userMapper.selectPage(p, wrapper));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> toggleUserStatus(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error(404, "用户不存在");
        user.setStatus(user.getStatus() != null && user.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        if (userMapper.selectById(id) == null) return Result.error(404, "用户不存在");
        userMapper.deleteById(id);
        return Result.success();
    }

    // ===== 商品管理 =====
    @GetMapping("/products")
    public Result<IPage<Product>> products(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int pageSize,
                                            @RequestParam(required = false) String keyword) {
        Page<Product> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(productMapper.selectPage(p, wrapper));
    }

    @PutMapping("/products/{id}/status")
    public Result<?> updateProductStatus(@PathVariable Long id, @RequestParam String status) {
        Product product = productMapper.selectById(id);
        if (product == null) return Result.error(404, "商品不存在");
        product.setStatus(status);
        productMapper.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        if (productMapper.selectById(id) == null) return Result.error(404, "商品不存在");
        productMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/products/selling")
    public Result<IPage<Product>> sellingProducts(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) {
        Page<Product> p = new Page<>(page, pageSize);
        return Result.success(productMapper.selectPage(p,
            new LambdaQueryWrapper<Product>().eq(Product::getStatus, "SELLING").orderByDesc(Product::getCreateTime)));
    }

    @GetMapping("/products/sold")
    public Result<IPage<Product>> soldProducts(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int pageSize) {
        Page<Product> p = new Page<>(page, pageSize);
        return Result.success(productMapper.selectPage(p,
            new LambdaQueryWrapper<Product>().eq(Product::getStatus, "SOLD").orderByDesc(Product::getCreateTime)));
    }

    // ===== 订单管理 =====
    @GetMapping("/transactions")
    public Result<IPage<Transaction>> transactions(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int pageSize) {
        Page<Transaction> p = new Page<>(page, pageSize);
        return Result.success(transactionMapper.selectPage(p,
            new LambdaQueryWrapper<Transaction>().orderByDesc(Transaction::getCreateTime)));
    }

    // ===== 举报管理 =====
    @GetMapping("/reports")
    public Result<IPage<Report>> reports(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int pageSize,
                                          @RequestParam(required = false) String status) {
        Page<Report> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreateTime);
        return Result.success(reportMapper.selectPage(p, wrapper));
    }

    @PutMapping("/reports/{id}/status")
    public Result<?> processReport(@PathVariable Long id, @RequestParam String status) {
        Report report = reportMapper.selectById(id);
        if (report == null) return Result.error(404, "举报不存在");
        report.setStatus(status);
        reportMapper.updateById(report);
        return Result.success();
    }

    // ===== 分类管理 =====
    @PostMapping("/categories")
    public Result<?> addCategory(@RequestBody Category category) {
        category.setId(null);
        categoryMapper.insert(category);
        return Result.success();
    }

    @PutMapping("/categories/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/categories/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }
}
