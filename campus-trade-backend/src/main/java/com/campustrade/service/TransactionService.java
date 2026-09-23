package com.campustrade.service;
import com.campustrade.entity.Transaction;
import com.campustrade.vo.TransactionDetailVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
public interface TransactionService {
    Long create(Long buyerId, Long productId);
    void updateStatus(Long userId, Long transactionId, String status);
    IPage<Transaction> getMyTransactions(Long userId, int page, int pageSize);
    Transaction getDetail(Long id);
    TransactionDetailVo getDetailWithInfo(Long id, Long currentUserId);
}
