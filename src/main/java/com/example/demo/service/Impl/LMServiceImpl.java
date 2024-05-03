package com.example.demo.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.JsonUtil;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.LuckyMoney;
import com.example.demo.entity.UserMoney;
import com.example.demo.mapper.LuckyMoneyMapper;
import com.example.demo.mapper.UserMoneyMapper;
import com.example.demo.service.LMService;
import com.example.demo.service.LuckyMoneyService;
import com.example.demo.service.UserMoneyService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LMServiceImpl extends ServiceImpl<LuckyMoneyMapper, LuckyMoney> implements LMService {
    @Autowired
    private LuckyMoneyMapper luckyMoneyMapper;
    @Autowired
    private LuckyMoneyService luckyMoneyService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    private UserMoneyMapper userMoneyMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Value("${spring.redis.channel.msgToAll}")
    private String msgToAll;
    @Autowired
    private  RedisTemplate<String,String>  redisTemplate;
    @Override
    @Transactional
    public void addLuckyMoney(long id, int num) {

        if(luckyMoneyMapper.selectById(id)!=null)
        {
            log.info("已经发过了");
            return;
        }
        LuckyMoney luckyMoney=new LuckyMoney();
        luckyMoney.setId(id);
        luckyMoney.setNum(num);
        int count = luckyMoneyMapper.insert(luckyMoney);
        if(count==0)
        {
            log.info("insert failed");
            return;
        }

    }

    @Override

    public void robLuckyMoney(ChatMessage chatMessage) {
        long id=chatMessage.getId();
        String sender=chatMessage.getSender();
        //判断红包数量
        LuckyMoney luckyMoney=luckyMoneyService.getById(id);
        if(luckyMoney==null)
        {
            log.info("红包不存在");
            return;
        }
        if (luckyMoney.getNum() <= 0) {
            log.info("红包已经被抢完了");
            return;
        }

        //一人一单
        //创造锁对象
        RLock lock = redissonClient.getLock("LuckyMoney:" + sender);

        //获取锁对象
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            log.info("已经抢过了");
            return;
        }
        try {
            //抢红包
            //创建当前类的代理对象？否则事务不生效
            LMServiceImpl currentProxy = (LMServiceImpl) AopContext.currentProxy();
            currentProxy.saveUserMoney(chatMessage);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
            finally {
            //释放锁
            lock.unlock();
        }



    }
    @Transactional
    public void saveUserMoney(ChatMessage chatMessage) {
        long id=chatMessage.getId();
        String sender=chatMessage.getSender();
        //判断该用户有没有下单过
        Integer count = userMoneyService.query()
                .eq("username", sender)
                .eq("money_id", id).count();
        if(count>0){
            log.info("已经抢过了!");
            return;
        }

        //乐观锁
        boolean success=luckyMoneyService.update()
                .setSql("num=num-1")
                .eq("id",id)
                .gt("num",0)
                .update();
        if (!success) {
            log.info("红包已经被抢完了");
            return;
        }

        //保存抢到的红包
        UserMoney userMoney=new UserMoney();
        userMoney.setUsername(sender);
        userMoney.setMoneyId(id);
        userMoneyMapper.insert(userMoney);

        chatMessage.setNum(luckyMoneyService.getById(id).getNum());
        chatMessage.setContent("robMoney, rest: "+chatMessage.getNum());
        try {
            redisTemplate.convertAndSend(msgToAll, JsonUtil.parseObjToJson(chatMessage));
        }catch (Exception e)
        {
            log.info(e.getMessage()+e);
        }
    }
}
