package com.campustrade.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.JwtUtil;
import com.campustrade.common.Result;
import com.campustrade.dto.LoginDto;
import com.campustrade.dto.RegisterDto;
import com.campustrade.entity.User;
import com.campustrade.mapper.UserMapper;
import com.campustrade.service.UserService;
import com.campustrade.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserMapper userMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @Override
    public Map<String,Object> login(LoginDto dto) {
        User user = null;
        // 先按手机号查找，再按用户名查找（兼容管理员）
        if (dto.getAccount().matches("\\d+")) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getAccount()));
        }
        if (user == null) {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getAccount()));
        }
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        UserInfoVo userInfo = new UserInfoVo();
        BeanUtils.copyProperties(user, userInfo);
        Map<String,Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", userInfo);
        return result;
    }

    @Override
    public void register(RegisterDto dto) {
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
            .eq(User::getPhone, dto.getPhone()));
        if (exist != null) throw new RuntimeException("该手机号已注册");
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // 自动生成用户名：u_完整手机号（手机号本身唯一，保证用户名唯一）
        user.setUsername("u_" + dto.getPhone());
                // 检查昵称是否唯一
        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            User nickExist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getNickname, dto.getNickname()));
            if (nickExist != null) throw new RuntimeException("昵称已被使用");
        }
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public UserInfoVo getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateProfile(Long userId, UserInfoVo vo) {
        // 检查昵称是否唯一
        if (vo.getNickname() != null && !vo.getNickname().isEmpty()) {
            User nickExist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getNickname, vo.getNickname()).ne(User::getId, userId));
            if (nickExist != null) throw new RuntimeException("昵称已被使用");
        }
        User user = new User();
        user.setId(userId);
        user.setNickname(vo.getNickname());
        user.setEmail(vo.getEmail());
        user.setPhone(vo.getPhone());
        user.setAvatar(vo.getAvatar());
        userMapper.updateById(user);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
