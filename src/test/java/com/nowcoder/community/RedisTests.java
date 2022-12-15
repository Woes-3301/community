package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String key = "test:count";

        redisTemplate.opsForValue().set(key,1);

        System.out.println(redisTemplate.opsForValue().get(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
        System.out.println(redisTemplate.opsForValue().decrement(key));
    }

    @Test
    public void testHashes(){
        String key = "test:user";

        redisTemplate.opsForHash().put(key,"id",1);
        redisTemplate.opsForHash().put(key,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(key,"id"));
        System.out.println(redisTemplate.opsForHash().get(key,"username"));
    }

    @Test
    public void testLists(){
        String key = "test:ids";

        redisTemplate.opsForList().leftPush(key,101);
        redisTemplate.opsForList().leftPush(key,102);
        redisTemplate.opsForList().leftPush(key,103);
        redisTemplate.opsForList().leftPush(key,104);

        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().index(key,0));
        System.out.println(redisTemplate.opsForList().range(key,0,2));

        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
    }

    @Test
    public void testSets(){
        String key = "test:teachers";

        redisTemplate.opsForSet().add(key,"赵","钱","孙","李");//无序

        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));//随机弹出一个
        System.out.println(redisTemplate.opsForSet().members(key));
    }

    @Test
    public void testSortedSets(){
        String key = "test:students";

        redisTemplate.opsForZSet().add(key,"A",90);
        redisTemplate.opsForZSet().add(key,"B",80);
        redisTemplate.opsForZSet().add(key,"D",60);
        redisTemplate.opsForZSet().add(key,"C",70);
        redisTemplate.opsForZSet().add(key,"E",50);

        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().score(key,"B"));
        System.out.println(redisTemplate.opsForZSet().rank(key,"B"));//排名，默认由小到大
        System.out.println(redisTemplate.opsForZSet().reverseRank(key,"B"));
        System.out.println(redisTemplate.opsForZSet().range(key,0,2));//默认由小到大
        System.out.println(redisTemplate.opsForZSet().reverseRange(key,0,2));
    }

    @Test
    public void testKeys(){
        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);//设置超时时间和单位
    }

    //多次访问同一个key
    @Test
    public void testBoundOperations(){
        String key = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(key);
        //BoundHashOperations、Bound****Operations
        //绑定key，之后使用operation，不需要再传入key

        operations.increment();
        operations.increment();
        operations.increment();

        System.out.println(operations.get());
    }

    //编程式事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String key = "test:tx";

                operations.multi();//启用事务

                operations.opsForSet().add(key,"zhan");
                operations.opsForSet().add(key,"li");
                operations.opsForSet().add(key,"wang");
                operations.opsForSet().add(key,"cai");

                System.out.println(operations.opsForSet().members(key));//redis不支持事务中的查询
                //它会在事务提交后，再次进行

                return operations.exec();//提交事务
            }
        });
        System.out.println(obj);
    }
}
