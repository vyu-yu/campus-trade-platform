package com.campustrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.entity.Product;
import com.campustrade.entity.Rating;
import com.campustrade.entity.Transaction;
import com.campustrade.mapper.CategoryMapper;
import com.campustrade.mapper.ProductMapper;
import com.campustrade.mapper.RatingMapper;
import com.campustrade.mapper.TransactionMapper;
import com.campustrade.mapper.UserMapper;
import com.campustrade.service.TransactionService;
import com.campustrade.vo.TransactionDetailVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired private TransactionMapper transactionMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private RatingMapper ratingMapper;

    @Override
    @Transactional
    public Long create(Long buyerId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new RuntimeException("商品不存在");
        if (!"SELLING".equals(product.getStatus())) throw new RuntimeException("该商品已下架或已售出");

        // Update product status to SOLD
        product.setStatus("SOLD");
        productMapper.updateById(product);

        // Create transaction
        Transaction tx = new Transaction();
        tx.setProductId(productId);
        tx.setSellerId(product.getUserId());
        tx.setBuyerId(buyerId);
        tx.setStatus("PENDING");
        transactionMapper.insert(tx);
        return tx.getId();
    }

   @Override
   public void updateStatus(Long userId, Long transactionId, String status) {
       Transaction tx = transactionMapper.selectById(transactionId);
       if (tx == null) throw new RuntimeException("交易不存在");

       // Only buyer or seller can update
       if (!tx.getBuyerId().equals(userId) && !tx.getSellerId().equals(userId)) {
           throw new RuntimeException("无权操作");
       }

       tx.setStatus(status);
       transactionMapper.updateById(tx);
         // If canceled, revert product to SELLING so it can be re-listed
         if ("CANCELLED".equals(status)) {
             Product product = productMapper.selectById(tx.getProductId());
             if (product != null) {
                 product.setStatus("SELLING");
                 productMapper.updateById(product);
             }
         }
   }

    @Override
    public IPage<Transaction> getMyTransactions(Long userId, int page, int pageSize) {
        Page<Transaction> p = new Page<>(page, pageSize);
        return transactionMapper.selectPage(p,
            new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getBuyerId, userId)
                .or()
                .eq(Transaction::getSellerId, userId)
                .orderByDesc(Transaction::getCreateTime));
    }

    @Override
    public Transaction getDetail(Long id) {
        return transactionMapper.selectById(id);
    }

    @Override
    public TransactionDetailVo getDetailWithInfo(Long id, Long currentUserId) {
        Transaction t = transactionMapper.selectById(id);
        if (t == null) throw new RuntimeException("交易不存在");

        TransactionDetailVo vo = new TransactionDetailVo();
        BeanUtils.copyProperties(t, vo);

        // Determine role
        if (t.getBuyerId().equals(currentUserId)) {
            vo.setMyRole("买家");
        } else if (t.getSellerId().equals(currentUserId)) {
            vo.setMyRole("卖家");
        } else {
            vo.setMyRole("未知");
        }

        // Product info
        Product product = productMapper.selectById(t.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductDescription(product.getDescription());
            vo.setProductPrice(product.getPrice());
            vo.setProductOriginalPrice(product.getOriginalPrice());
            vo.setProductCondition(product.getCondition());
            vo.setProductImages(product.getImages());
            if (product.getCategoryId() != null) {
                var cat = categoryMapper.selectById(product.getCategoryId());
                if (cat != null) vo.setProductCategoryName(cat.getName());
            }
        }

        // Seller info
        var seller = userMapper.selectById(t.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerPhone(seller.getPhone());
            vo.setSellerAvatar(seller.getAvatar());
        }

        // Rating info (only for completed transactions)
        if ("COMPLETED".equals(t.getStatus())) {
            Rating rating = ratingMapper.selectOne(
                new LambdaQueryWrapper<Rating>()
                    .eq(Rating::getTransactionId, t.getId())
                    .eq(Rating::getUserId, currentUserId));
                    vo.setMyRating(rating != null ? rating.getScore() : null);
                    vo.setMyRatingId(rating != null ? rating.getId() : null);
                    vo.setMyRatingContent(rating != null ? rating.getContent() : null);

            List<Rating> sellerRatings = ratingMapper.selectList(
                new LambdaQueryWrapper<Rating>()
                    .eq(Rating::getSellerId, t.getSellerId()));
            double avgScore = sellerRatings.stream().mapToInt(Rating::getScore).average().orElse(0);
            vo.setSellerAvgRating(Math.round(avgScore * 10) / 10.0);
            vo.setSellerRatingCount(sellerRatings.size());
        }

        return vo;
    }
}
