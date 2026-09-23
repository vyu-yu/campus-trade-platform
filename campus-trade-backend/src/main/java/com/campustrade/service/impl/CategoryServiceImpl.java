package com.campustrade.service.impl;
import com.campustrade.entity.Category;
import com.campustrade.mapper.CategoryMapper;
import com.campustrade.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired private CategoryMapper categoryMapper;
    @Override
    public List<Category> getAll() {
        return categoryMapper.selectList(null);
    }
    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
    }
}
