package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.dto.ProductPublishDto;
import com.campustrade.dto.ProductSearchDto;
import com.campustrade.service.ProductService;
import com.campustrade.vo.ProductDetailVo;
import com.campustrade.vo.ProductListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired private ProductService productService;
    @PostMapping("/publish")
    public Result<Long> publish(Authentication auth, @Valid @RequestBody ProductPublishDto dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(productService.publish(userId, dto));
    }
    @PutMapping("/{id}")
    public Result<?> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody ProductPublishDto dto) {
        Long userId = (Long) auth.getPrincipal();
        productService.update(userId, id, dto);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        productService.delete(userId, id);
        return Result.success();
    }
    @GetMapping("/{id}")
    public Result<ProductDetailVo> detail(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(productService.getDetail(id, userId));
    }
    @GetMapping("/list")
    public Result<IPage<ProductListVo>> list(ProductSearchDto dto) {
        return Result.success(productService.getList(dto));
    }
    @GetMapping("/my")
    public Result<IPage<ProductListVo>> my(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(productService.getMyProducts(userId, page, pageSize));
    }
    @GetMapping("/sold-by-seller/{sellerId}")
    public Result<IPage<ProductListVo>> soldBySeller(@PathVariable Long sellerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(productService.getSoldBySeller(sellerId, page, pageSize));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(Authentication auth, @PathVariable Long id, @RequestParam String status) {
        Long userId = (Long) auth.getPrincipal();
        productService.updateStatus(userId, id, status);
        return Result.success();
    }
}
