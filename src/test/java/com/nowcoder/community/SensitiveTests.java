package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "小黑子欢乐梦工厂，速来搞黄色。";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "小♥黑子♥欢♥乐♥梦♥工♥厂♥，速来搞♥黄色。";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
