package com.campustrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.entity.Message;
import com.campustrade.entity.User;
import com.campustrade.mapper.MessageMapper;
import com.campustrade.mapper.UserMapper;
import com.campustrade.service.MessageService;
import com.campustrade.vo.MessageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired private MessageMapper messageMapper;
    @Autowired private UserMapper userMapper;

    @Override
    public void send(Long userId, Long productId, String content, Long parentId) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setProductId(productId);
        msg.setContent(content);
        msg.setParentId(parentId);
        messageMapper.insert(msg);
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        messageMapper.delete(new LambdaQueryWrapper<Message>()
            .eq(Message::getId, messageId)
            .eq(Message::getUserId, userId));
    }

    @Override
    public IPage<MessageVo> getByProduct(Long productId, int page, int pageSize) {
        Page<Message> msgPage = messageMapper.selectPage(new Page<>(page, pageSize),
            new LambdaQueryWrapper<Message>().eq(Message::getProductId, productId)
                .orderByDesc(Message::getCreateTime));

        Page<MessageVo> result = new Page<>(msgPage.getCurrent(), msgPage.getSize(), msgPage.getTotal());
        result.setRecords(msgPage.getRecords().stream().map(msg -> {
            MessageVo vo = new MessageVo();
            BeanUtils.copyProperties(msg, vo);
            User user = userMapper.selectById(msg.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }
}
