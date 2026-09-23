package com.campustrade.service;
import com.campustrade.entity.Message;
import com.campustrade.vo.MessageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
public interface MessageService {
    void send(Long userId, Long productId, String content, Long parentId);
    void deleteMessage(Long userId, Long messageId);
    IPage<MessageVo> getByProduct(Long productId, int page, int pageSize);
}
