package com.campustrade.controller;

import com.campustrade.common.Result;
import com.campustrade.dto.LoginDto;
import com.campustrade.dto.RegisterDto;
import com.campustrade.service.UserService;
import com.campustrade.vo.UserInfoVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDto dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@Valid @RequestBody LoginDto dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/profile")
    public Result<UserInfoVo> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @GetMapping("/profile/public")
    public Result<UserInfoVo> publicProfile(@RequestParam Long id) {
        return Result.success(userService.getProfile(id));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(Authentication auth, @RequestBody UserInfoVo vo) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateProfile(userId, vo);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
}