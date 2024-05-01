package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LuckyMoneyMapper extends BaseMapper<LuckyMoney> {
}
