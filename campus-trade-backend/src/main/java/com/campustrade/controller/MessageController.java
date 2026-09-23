package com.campustrade.controller;
import com.campustrade.common.Result;
import com.campustrade.service.MessageService;
import com.campustrade.vo.MessageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Autowired private MessageService messageService;
    @PostMapping
    public Result<?> send(Authentication auth, @RequestParam Long productId,
            @RequestParam String content, @RequestParam(required = false) Long parentId) {
        Long userId = (Long) auth.getPrincipal();
        messageService.send(userId, productId, content, parentId);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        messageService.deleteMessage(userId, id);
        return Result.success();
    }

    @GetMapping("/{productId}")
    public Result<IPage<MessageVo>> list(@PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(messageService.getByProduct(productId, page, pageSize));
    }
}
