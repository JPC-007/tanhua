package com.tanhua.dubbo;

import com.tanhua.domain.mongo.Visitor;
import com.tanhua.dubbo.api.mongo.VisitorsApi;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestVisitors {

    @Autowired
    private VisitorsApi visitorApi;

    @Test
    public void testSave(){
        for (int i = 0; i < 100; i++) {
            Visitor visitor = new Visitor();
            visitor.setFrom("首页");
            visitor.setUserId(1l);//用户id
            visitor.setScore(77d);
            visitor.setVisitorUserId(RandomUtils.nextLong(11,50));
            this.visitorApi.save(visitor);
        }
        System.out.println("ok");
    }
}