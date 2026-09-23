package com.campustrade.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.dto.ProductPublishDto;
import com.campustrade.dto.ProductSearchDto;
import com.campustrade.entity.Category;
import com.campustrade.entity.Favorite;
import com.campustrade.entity.Product;
 import com.campustrade.entity.User;
 import com.campustrade.entity.Transaction;
import com.campustrade.mapper.CategoryMapper;
import com.campustrade.mapper.FavoriteMapper;
import com.campustrade.mapper.TransactionMapper;
import com.campustrade.mapper.ProductMapper;
import com.campustrade.mapper.UserMapper;
import com.campustrade.service.ProductService;
import com.campustrade.vo.ProductDetailVo;
import com.campustrade.vo.ProductListVo;
import com.campustrade.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired private ProductMapper productMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired
    private TransactionMapper transactionMapper;
    @Override
    public Long publish(Long userId, ProductPublishDto dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setUserId(userId);
        product.setStatus("SELLING");
        product.setViewCount(0);
        productMapper.insert(product);
        return product.getId();
    }
    @Override
    public void update(Long userId, Long productId, ProductPublishDto dto) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new RuntimeException("商品不存在");
        if (!product.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        BeanUtils.copyProperties(dto, product);
        product.setId(productId);
        productMapper.updateById(product);
    }
    @Override
    public void delete(Long userId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new RuntimeException("商品不存在");
        if (!product.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        productMapper.deleteById(productId);
    }
    @Override
    public ProductDetailVo getDetail(Long productId, Long currentUserId) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new RuntimeException("商品不存在");
        product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
        productMapper.updateById(product);
        ProductDetailVo vo = new ProductDetailVo();
        BeanUtils.copyProperties(product, vo);
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
            vo.setCategoryId(category.getId());
        }
        User seller = userMapper.selectById(product.getUserId());
        if (seller != null) {
            UserInfoVo sellerVo = new UserInfoVo();
            BeanUtils.copyProperties(seller, sellerVo);
            vo.setSeller(sellerVo);
        }
        if (currentUserId != null) {
            Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, currentUserId).eq(Favorite::getProductId, productId));
            vo.setFavorited(count > 0);
        }
        // Set buyer info for sold products
   if ("SOLD".equals(product.getStatus())) {
       Transaction tx = transactionMapper.selectOne(
           new LambdaQueryWrapper<Transaction>()
               .eq(Transaction::getProductId, productId)
               .orderByDesc(Transaction::getCreateTime)
               .last("LIMIT 1"));
        if (tx != null && "COMPLETED".equals(tx.getStatus())) {
                User buyer = userMapper.selectById(tx.getBuyerId());
                if (buyer != null) {
                    if (buyer.getNickname() != null && !buyer.getNickname().isEmpty()) {
                        vo.setBuyerNickname(buyer.getNickname());
                    } else {
                        vo.setBuyerNickname(buyer.getUsername());
                    }
                    vo.setBuyerPhone(buyer.getPhone());
       }
        // Seller-marked as sold (no completed transaction), show seller
        if (vo.getBuyerNickname() == null) {
            if (vo.getSeller() != null) {
                String sellerName = vo.getSeller().getNickname() != null ? vo.getSeller().getNickname() : vo.getSeller().getUsername();
                vo.setBuyerNickname(sellerName + "（卖家标记已售）");
            } else {
                vo.setBuyerNickname("卖家标记已售");
            }
        }
   }
        }
        return vo;
    }
    @Override
    public IPage<ProductListVo> getList(ProductSearchDto dto) {
        Page<Product> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, "SELLING");
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Product::getTitle, dto.getKeyword())
                .or().like(Product::getDescription, dto.getKeyword()));
        }
        if (dto.getCategoryId() != null) wrapper.eq(Product::getCategoryId, dto.getCategoryId());
        if (dto.getUserId() != null) wrapper.eq(Product::getUserId, dto.getUserId());
        if (StringUtils.hasText(dto.getCondition())) wrapper.eq(Product::getCondition, dto.getCondition());
        if (dto.getMinPrice() != null) wrapper.ge(Product::getPrice, dto.getMinPrice());
        if (dto.getMaxPrice() != null) wrapper.le(Product::getPrice, dto.getMaxPrice());
        if ("price".equals(dto.getSortBy())) {
            wrapper.orderBy(true, "asc".equals(dto.getSortOrder()), Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }
        IPage<Product> productPage = productMapper.selectPage(page, wrapper);
        IPage<ProductListVo> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(productPage.getRecords().stream().map(p -> {
            ProductListVo vo = new ProductListVo();
            BeanUtils.copyProperties(p, vo);
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                vo.setImages(p.getImages().split(",")[0]);
            }
            Category cat = categoryMapper.selectById(p.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
            User seller = userMapper.selectById(p.getUserId());
            if (seller != null) vo.setSellerNickname(seller.getNickname());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
    @Override
    public IPage<ProductListVo> getMyProducts(Long userId, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getUserId, userId).orderByDesc(Product::getCreateTime);
        IPage<Product> productPage = productMapper.selectPage(page, wrapper);
        IPage<ProductListVo> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        voPage.setRecords(productPage.getRecords().stream().map(p -> {
            ProductListVo vo = new ProductListVo();
            BeanUtils.copyProperties(p, vo);
            if (p.getImages() != null && !p.getImages().isEmpty()) {
                vo.setImages(p.getImages().split(",")[0]);
            }
            Category cat = categoryMapper.selectById(p.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
    @Override
    public IPage<ProductListVo> getSoldBySeller(Long sellerId, int pageNum, int pageSize) {
        Page<Transaction> txPage = new Page<>(pageNum, pageSize);
        IPage<Transaction> completedTxPage = transactionMapper.selectPage(txPage,
            new LambdaQueryWrapper<Transaction>()
                .eq(Transaction::getSellerId, sellerId)
                .eq(Transaction::getStatus, "COMPLETED")
                .orderByDesc(Transaction::getCreateTime));
        IPage<ProductListVo> voPage = new Page<>(txPage.getCurrent(), txPage.getSize(), txPage.getTotal());
        voPage.setRecords(completedTxPage.getRecords().stream().map(tx -> {
            Product p = productMapper.selectById(tx.getProductId());
            ProductListVo vo = new ProductListVo();
            if (p != null) {
                BeanUtils.copyProperties(p, vo);
                com.campustrade.entity.Category cat = categoryMapper.selectById(p.getCategoryId());
                if (cat != null) vo.setCategoryName(cat.getName());
            }
            User buyer = userMapper.selectById(tx.getBuyerId());
            if (buyer != null) {
                vo.setBuyerNickname(buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername());
                vo.setBuyerPhone(buyer.getPhone());
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public void updateStatus(Long userId, Long productId, String status) {
        Product product = productMapper.selectById(productId);
        if (product == null) throw new RuntimeException("商品不存在");
        if (!product.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        product.setStatus(status);
        productMapper.updateById(product);
    }
}

