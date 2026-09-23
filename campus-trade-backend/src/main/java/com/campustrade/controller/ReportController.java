package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.dto.ReportSubmitDto;
import com.campustrade.entity.Report;
import com.campustrade.service.ReportService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired private ReportService reportService;
    @PostMapping
    public Result<?> submit(Authentication auth, @Valid @RequestBody ReportSubmitDto dto) {
        Long userId = (Long) auth.getPrincipal();
        reportService.submit(userId, dto);
        return Result.success();
    }
    @GetMapping("/list")
    public Result<IPage<Report>> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(reportService.getList(page, pageSize));
    }
    @PutMapping("/{id}/status")
    public Result<?> handle(@PathVariable Long id, @RequestParam String status) {
        reportService.handleReport(id, status);
        return Result.success();
    }
}
