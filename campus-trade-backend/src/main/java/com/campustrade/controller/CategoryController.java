package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.entity.Category;
import com.campustrade.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired private CategoryService categoryService;
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.getAll());
    }
    @PostMapping
    public Result<?> add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success();
    }
}
