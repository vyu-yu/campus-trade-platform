package com.campustrade.service;
import com.campustrade.dto.ProductPublishDto;
import com.campustrade.dto.ProductSearchDto;
import com.campustrade.entity.Product;
import com.campustrade.vo.ProductDetailVo;
import com.campustrade.vo.ProductListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
public interface ProductService {
    Long publish(Long userId, ProductPublishDto dto);
    void update(Long userId, Long productId, ProductPublishDto dto);
    void delete(Long userId, Long productId);
    ProductDetailVo getDetail(Long productId, Long currentUserId);
    IPage<ProductListVo> getList(ProductSearchDto dto);
    IPage<ProductListVo> getMyProducts(Long userId, int page, int pageSize);
    IPage<ProductListVo> getSoldBySeller(Long sellerId, int pageNum, int pageSize);
    void updateStatus(Long userId, Long productId, String status);
}
