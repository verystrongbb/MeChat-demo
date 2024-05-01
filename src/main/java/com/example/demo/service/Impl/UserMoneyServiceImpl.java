package com.example.demo.service.Impl;

import com.example.demo.service.UserMoneyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserMoneyServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.example.demo.mapper.UserMoneyMapper, com.example.demo.entity.UserMoney> implements UserMoneyService{
}
