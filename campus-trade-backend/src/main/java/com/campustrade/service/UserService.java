package com.campustrade.service;

import com.campustrade.dto.LoginDto;
import com.campustrade.dto.RegisterDto;
import com.campustrade.entity.User;
import com.campustrade.vo.UserInfoVo;
import java.util.Map;

public interface UserService {
    Map<String,Object> login(LoginDto dto);
    void register(RegisterDto dto);
    UserInfoVo getProfile(Long userId);
    void updateProfile(Long userId, UserInfoVo vo);
    User getById(Long id);
    void changePassword(Long userId, String oldPassword, String newPassword);
}