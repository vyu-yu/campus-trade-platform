package com.campustrade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campustrade.common.Result;
import com.campustrade.entity.*;
import com.campustrade.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private ConversationMapper conversationMapper;
    @Autowired private ChatMessageMapper chatMessageMapper;
    @Autowired private UserMapper userMapper;

    // Get or create conversation
    @PostMapping("/conversation")
    public Result<Map<String,Object>> getOrCreate(Authentication auth, @RequestBody Map<String,Long> body) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        Long sellerId = body.get("sellerId");
        Long productId = body.get("productId");

        // Find existing conversation
        Conversation exist = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
            .eq(Conversation::getBuyerId, userId).eq(Conversation::getSellerId, sellerId)
            .eq(Conversation::getProductId, productId));
        if (exist == null) {
            exist = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getBuyerId, sellerId).eq(Conversation::getSellerId, userId)
                .eq(Conversation::getProductId, productId));
        }
        if (exist == null) {
            exist = new Conversation();
            exist.setBuyerId(userId);
            exist.setSellerId(sellerId);
            exist.setProductId(productId);
            exist.setBuyerUnread(0);
            exist.setSellerUnread(0);
            exist.setCreateTime(LocalDateTime.now());
            conversationMapper.insert(exist);
        }

        Map<String,Object> data = new HashMap<>();
        data.put("conversationId", exist.getId());
        data.put("sellerId", exist.getSellerId());
        data.put("buyerId", exist.getBuyerId());
        return Result.success(data);
    }

    // Send message
    @PostMapping("/send")
    public Result<?> send(Authentication auth, @RequestParam Long conversationId, @RequestParam String content) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return Result.error(404, "对话不存在");

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderId(userId);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(msg);

        // Update conversation
        conv.setLastMessage(content);
        conv.setLastTime(LocalDateTime.now());
        // Increment unread for the other party
        if (userId.equals(conv.getBuyerId())) {
            conv.setSellerUnread(conv.getSellerUnread() == null ? 1 : conv.getSellerUnread() + 1);
        } else {
            conv.setBuyerUnread(conv.getBuyerUnread() == null ? 1 : conv.getBuyerUnread() + 1);
        }
        conversationMapper.updateById(conv);
        return Result.success();
    }

    // List conversations for current user
    @GetMapping("/conversations")
    public Result<List<Map<String,Object>>> conversations(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        List<Conversation> list = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
            .and(w -> w.eq(Conversation::getBuyerId, userId).or().eq(Conversation::getSellerId, userId))
            .orderByDesc(Conversation::getLastTime));

        List<Map<String,Object>> result = new ArrayList<>();
        for (Conversation c : list) {
            Map<String,Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("productId", c.getProductId());
            item.put("lastMessage", c.getLastMessage());
            item.put("lastTime", c.getLastTime());

            Long otherId = c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId();
            User other = userMapper.selectById(otherId);
            item.put("otherUserId", otherId);
            item.put("otherNickname", other != null ? (other.getNickname() != null ? other.getNickname() : other.getUsername()) : "未知用户");

            int unread = c.getBuyerId().equals(userId) ? (c.getBuyerUnread() != null ? c.getBuyerUnread() : 0)
                                                        : (c.getSellerUnread() != null ? c.getSellerUnread() : 0);
            item.put("unread", unread);
            result.add(item);
        }
        return Result.success(result);
    }

    // Get messages in a conversation
    @GetMapping("/messages/{conversationId}")
    public Result<IPage<ChatMessage>> messages(@PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        Page<ChatMessage> p = new Page<>(page, pageSize);
        return Result.success(chatMessageMapper.selectPage(p,
            new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreateTime)));
    }

    // Mark conversation as read
    @PutMapping("/read/{conversationId}")
    public Result<?> markRead(Authentication auth, @PathVariable Long conversationId) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return Result.error(404, "对话不存在");
        if (userId.equals(conv.getBuyerId())) {
            conv.setBuyerUnread(0);
        } else {
            conv.setSellerUnread(0);
        }
        conversationMapper.updateById(conv);
        return Result.success();
    }

    // Get unread count
    @GetMapping("/unread")
    public Result<Map<String,Integer>> unread(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        List<Conversation> list = conversationMapper.selectList(
            new LambdaQueryWrapper<Conversation>()
                .and(w -> w.eq(Conversation::getBuyerId, userId).or().eq(Conversation::getSellerId, userId)));

        int total = 0;
        for (Conversation c : list) {
            if (userId.equals(c.getBuyerId())) {
                total += (c.getBuyerUnread() != null ? c.getBuyerUnread() : 0);
            } else {
                total += (c.getSellerUnread() != null ? c.getSellerUnread() : 0);
            }
        }
        Map<String,Integer> data = new HashMap<>();
        data.put("total", total);
        return Result.success(data);
    }

    private Long getUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return (Long) auth.getPrincipal();
    }

}
