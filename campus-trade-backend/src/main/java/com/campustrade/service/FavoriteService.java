package com.campustrade.service;
import com.campustrade.vo.ProductListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
public interface FavoriteService {
    void add(Long userId, Long productId);
    void remove(Long userId, Long productId);
    boolean isFavorited(Long userId, Long productId);
    IPage<ProductListVo> getMyFavorites(Long userId, int page, int pageSize);
}
