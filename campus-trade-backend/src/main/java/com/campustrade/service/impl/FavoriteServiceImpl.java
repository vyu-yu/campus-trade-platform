package com.campustrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.entity.Favorite;
import com.campustrade.entity.Product;
import com.campustrade.entity.User;
import com.campustrade.mapper.FavoriteMapper;
import com.campustrade.mapper.ProductMapper;
import com.campustrade.mapper.UserMapper;
import com.campustrade.service.FavoriteService;
import com.campustrade.vo.ProductListVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private UserMapper userMapper;

    @Override
    public void add(Long userId, Long productId) {
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getProductId, productId));
        if (exist == null) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setProductId(productId);
            favoriteMapper.insert(fav);
        }
    }

    @Override
    public void remove(Long userId, Long productId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getProductId, productId));
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getProductId, productId)) > 0;
    }

    @Override
    public IPage<ProductListVo> getMyFavorites(Long userId, int page, int pageSize) {
        Page<Favorite> favPage = favoriteMapper.selectPage(new Page<>(page, pageSize),
            new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));

        Page<ProductListVo> result = new Page<>(favPage.getCurrent(), favPage.getSize(), favPage.getTotal());
        result.setRecords(favPage.getRecords().stream().map(fav -> {
            Product product = productMapper.selectById(fav.getProductId());
            if (product == null) return null;
            ProductListVo vo = new ProductListVo();
            BeanUtils.copyProperties(product, vo);
            return vo;
        }).filter(v -> v != null).collect(Collectors.toList()));
        return result;
    }
}
