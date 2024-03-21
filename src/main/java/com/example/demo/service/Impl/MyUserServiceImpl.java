package com.example.demo.service.Impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.MyUser;
import com.example.demo.mapper.MyUserMapper;
import com.example.demo.service.MyUserService;
import org.springframework.stereotype.Service;

@Service
public class MyUserServiceImpl extends ServiceImpl<MyUserMapper, MyUser> implements MyUserService {
}
