package com.tanhua.server.test;

import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.server.service.TestUserInfoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MySpringCacheTest {

    @Autowired
    private TestUserInfoService testUserInfoService;


    @Test
    public void testFindAll() throws IOException {
        List<UserInfo> list = testUserInfoService.findAll();
        for (UserInfo userInfo : list) {
            System.out.println(userInfo);
        }

    }

    @Test
    public void testFindById() throws IOException {
        UserInfo userInfo = testUserInfoService.findById(1l);
        System.out.println(userInfo);
    }

    @Test
    public void testSaveUser(){
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1l);
        testUserInfoService.save(userInfo);
    }

    @Test
    public void testUpdateUser(){
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1l);
        testUserInfoService.update(userInfo);
    }

}