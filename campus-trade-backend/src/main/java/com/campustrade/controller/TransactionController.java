package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.entity.Transaction;
import com.campustrade.service.TransactionService;
import com.campustrade.vo.TransactionDetailVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    @Autowired private TransactionService transactionService;
    @PostMapping("/create")
    public Result<Long> create(Authentication auth, @RequestParam Long productId) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(transactionService.create(userId, productId));
    }
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(Authentication auth, @PathVariable Long id, @RequestParam String status) {
        Long userId = (Long) auth.getPrincipal();
        transactionService.updateStatus(userId, id, status);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<IPage<Transaction>> list(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(transactionService.getMyTransactions(userId, page, pageSize));
    }
    @GetMapping("/{id}")
    public Result<TransactionDetailVo> detail(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(transactionService.getDetailWithInfo(id, userId));
    }
}
