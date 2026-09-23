package com.campustrade.service;
import com.campustrade.entity.Category;
import java.util.List;
public interface CategoryService {
    List<Category> getAll();
    void add(Category category);
}
