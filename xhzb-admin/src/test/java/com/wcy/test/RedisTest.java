package com.wcy.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void test01(){
        // 单行存储
        redisTemplate.opsForValue().set("name","小汪"); //设置值
        System.out.println(redisTemplate.opsForValue().get("name"));//获取值

        // 批量存储,需要传入map
        redisTemplate.opsForValue().multiSet(Map.of("age","18","sex","男人哦"));
        // 批量取,使用list
        List<String> strings = redisTemplate.opsForValue().multiGet(List.of("name", "age", "sex"));
        System.out.println(strings);


        Long counter = redisTemplate.opsForValue().increment("count");//按1的步长自增
        System.out.println(counter);
        counter = redisTemplate.opsForValue().increment("count",3);//按3的步长自增
        System.out.println(counter);

        counter = redisTemplate.opsForValue().decrement("count");//按1的步长自减
        System.out.println(counter);
        counter = redisTemplate.opsForValue().decrement("count",2);//按2的步长自增减
        System.out.println(counter);

        // 如有有则不做操作,返回布尔值
        Boolean b = this.redisTemplate.opsForValue().setIfAbsent("name", "我是无敌帅汪");
        System.out.println(b);

        Boolean b1 = this.redisTemplate.opsForValue().setIfAbsent("name2", "我是无敌帅黄");
        System.out.println(b1);

        // 设置过期时间
        this.redisTemplate.opsForValue().set("code","abc123", Duration.ofMinutes(3));
    }

    @Test
    void testHash(){
        // 测试hash的读写方法
        this.redisTemplate.opsForHash().put("wcy:001","name","汪宸宇");
        // 读取
        Object name = this.redisTemplate.opsForHash().get("wcy:001", "name");
        System.out.println("读取name字段的结果是:"+ name);

        // 批量存储
        this.redisTemplate.opsForHash().putAll("wcy:001",Map.of("age","18","gender","我是人妖"));
        // 批量读取
        List<Object> list = this.redisTemplate.opsForHash().multiGet("wcy:001", List.of("name", "age", "gender"));
        System.out.println(list);

        // 删除字段
        this.redisTemplate.opsForHash().delete("wcy:001","age");

        // 获取所有key
        Set<Object> keys = this.redisTemplate.opsForHash().keys("wcy:001");
        System.out.println(keys);

        // 获取所有values
        List<Object> values = this.redisTemplate.opsForHash().values("wcy:001");
        System.out.println(values);

        // 获取键值对
        Map<Object, Object> entries = this.redisTemplate.opsForHash().entries("wcy:001");
        System.out.println(entries);
    }

    @Test
    void testList(){
        // 测试list集合
        this.redisTemplate.opsForList().leftPush("郭峰list","刘浩");
        // 批量添加
        this.redisTemplate.opsForList().leftPushAll("郭峰list","菲比丘比","弗洛洛","千咲");

        // 截取读取
        List<String> list = this.redisTemplate.opsForList().range("郭峰list", 0, -1);
        System.out.println(list);

        // 弹出元素
    }

    // 集合相关
    @Test
    void testSet(){
        this.redisTemplate.opsForSet().add("nameSet","郭峰","猛虎王","狂野猩","奥利奥");
        // 获取成员数量
        Long size = this.redisTemplate.opsForSet().size("nameSet");
        System.out.println(size);

        // 获取成员
        Set<String> nameSet = this.redisTemplate.opsForSet().members("nameSet");
        System.out.println(nameSet);


        // 搞第二个集合
        this.redisTemplate.opsForSet().add("nameSet2","狂野猩","奥利奥","龙卷风","成龙");
        // 交并差
        Set<String> intersect = this.redisTemplate.opsForSet().intersect("nameSet", "nameSet2");
        System.out.println(intersect);

        Set<String> union = this.redisTemplate.opsForSet().union("nameSet", "nameSet2");
        System.out.println(union);

        Set<String> difference = this.redisTemplate.opsForSet().difference("nameSet", "nameSet2");
        System.out.println(difference);

        // 移除
        this.redisTemplate.opsForSet().remove("nameSet","狂野猩");
    }

    @Test
    void testCommon(){
        // 判断是否存在
        System.out.println(redisTemplate.hasKey("wcy:001"));

        // 模糊查询
        Set<String> keys = this.redisTemplate.keys("*");
        System.out.println(keys);

        // 获取类型
        DataType type = this.redisTemplate.type("wcy:001");
        System.out.println(type);

    }

    @Test
    void scoreTest(){
        // 添加元素
        this.redisTemplate.opsForZSet().add("rank","汪宸宇",100d);

    }
}
