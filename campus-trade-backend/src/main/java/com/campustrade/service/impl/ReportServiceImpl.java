package com.campustrade.service.impl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.dto.ReportSubmitDto;
import com.campustrade.entity.Product;
import com.campustrade.entity.Report;
import com.campustrade.mapper.ProductMapper;
import com.campustrade.mapper.ReportMapper;
import com.campustrade.service.ReportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired private ReportMapper reportMapper;
    @Autowired private ProductMapper productMapper;
    @Override
    public void submit(Long userId, ReportSubmitDto dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) throw new RuntimeException("?????");
        Report report = new Report();
        BeanUtils.copyProperties(dto, report);
        report.setUserId(userId);
        report.setStatus("PENDING");
        reportMapper.insert(report);
    }
    @Override
    public IPage<Report> getList(int pageNum, int pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        return reportMapper.selectPage(page, null);
    }
    @Override
    public void handleReport(Long reportId, String status) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new RuntimeException("?????");
        report.setStatus(status);
        reportMapper.updateById(report);
        if ("RESOLVED".equals(status)) {
            Product product = productMapper.selectById(report.getProductId());
            if (product != null) {
                product.setStatus("TAKEN_DOWN");
                productMapper.updateById(product);
            }
        }
    }
}
