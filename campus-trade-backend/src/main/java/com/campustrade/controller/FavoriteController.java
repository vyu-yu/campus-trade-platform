package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.service.FavoriteService;
import com.campustrade.vo.ProductListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Autowired private FavoriteService favoriteService;
    @PostMapping
    public Result<?> add(Authentication auth, @RequestParam Long productId) {
        Long userId = (Long) auth.getPrincipal();
        favoriteService.add(userId, productId);
        return Result.success();
    }
    @DeleteMapping("/{productId}")
    public Result<?> remove(Authentication auth, @PathVariable Long productId) {
        Long userId = (Long) auth.getPrincipal();
        favoriteService.remove(userId, productId);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<IPage<ProductListVo>> list(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(favoriteService.getMyFavorites(userId, page, pageSize));
    }
}
