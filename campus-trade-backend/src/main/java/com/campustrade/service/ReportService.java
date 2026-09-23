package com.campustrade.service;
import com.campustrade.dto.ReportSubmitDto;
import com.campustrade.entity.Report;
import com.baomidou.mybatisplus.core.metadata.IPage;
public interface ReportService {
    void submit(Long userId, ReportSubmitDto dto);
    IPage<Report> getList(int page, int pageSize);
    void handleReport(Long reportId, String status);
}
