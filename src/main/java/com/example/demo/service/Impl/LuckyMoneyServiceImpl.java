package com.example.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.SnowFlakeUtil;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;
import com.example.demo.mapper.LuckyMoneyMapper;
import com.example.demo.service.LuckyMoneyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LuckyMoneyServiceImpl extends ServiceImpl<LuckyMoneyMapper, LuckyMoney> implements LuckyMoneyService {

}
