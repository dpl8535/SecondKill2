package com.atguigu.sk.controller;

import com.atguigu.sk.utils.JedisPoolUtil;
import jdk.nashorn.internal.scripts.JD;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

/**
 * @author dplStart
 * @create 下午 09:38
 * @Description
 */
@RestController
public class SecondsController {

    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + " return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + " return 0;\r\n"
            + "else \r\n"
            + " redis.call(\"decr\",qtkey);\r\n"
            + " redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";

    @PostMapping(value = "/res/doSecondsKill", produces = "text/html;charset=UTF-8")
    public String doSkByLUA(Integer id) {
        //随机生成用户id
        Integer usrid = (int) (10000 * Math.random());
//        Jedis jedis = new Jedis("192.168.37.129", 6379);
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        //加载LUA脚本
        String sha1 = jedis.scriptLoad(secKillScript);
        //将LUA脚本和LUA脚本需要的参数传给redis执行：keyCount：lua脚本需要的参数数量，params：参数列表
        Object obj = jedis.evalsha(sha1, 2, usrid + "", id + "");
        // Long 强转为Integer会报错 ，Lange和Integer没有父类和子类的关系
        int result = (int) ((long) obj);
        if (result == 1) {
            System.out.println("秒杀成功");
            return "ok";
        } else if (result == 2) {
            System.out.println("重复秒杀");
            return "重复秒杀";
        } else {
            System.out.println("库存不足");
            return "库存不足";
        }
    }

    /*@PostMapping(value = "/res/doSecondsKill", produces = "text/html;charset=UTF-8")
    public String doSecondsKill(Integer id) {

        //用户id
        Integer usrid = (int) (10000 * Math.random());
        //商品id
        Integer pid = id;

        //连接Redis
        Jedis jedis = new Jedis("192.168.37.129", 6379);

        //拼接用户key 商品key
        String qtKey = "sk:" + pid + ":qt";
        String usrKey = "sk:" + pid + ":usr";

        if (jedis.sismember(usrKey, usrid + "")) {
            System.err.println("重复秒杀：" + usrid);
            return "该用户已经秒杀过，请勿重复秒杀";
        }
        //判断库存
        //对key进行watch
        jedis.watch(qtKey);
        String qtStr = jedis.get(qtKey);

        if (StringUtils.isEmpty(qtStr)) {
            System.err.println("秒杀尚未开始");
            return "秒杀尚未开始";
        }

        int qtNum = Integer.parseInt(qtStr);
        if (qtNum <= 0) {
            System.err.println("库存不足");
            return "库存不足";
        }

        //进行秒杀业务
        Transaction multi = jedis.multi();//开启组队
        multi.decr(qtKey);

        multi.sadd(usrKey,usrid + "");
        multi.exec();
        System.out.println(usrid + "秒杀成功!");

        return "ok";
    }*/

}
