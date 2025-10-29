package com.tanhua.manage.test;

import com.tanhua.commons.templates.HuaWeiUGCTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HuaWeiTest {

    @Autowired
    private HuaWeiUGCTemplate template;

    @Test
    public void testToken() {
        System.out.println(template.getToken());
    }

    @Test
    public void testText() {
        boolean check = template.textContentCheck("好好先生");
        System.out.println(check);

        boolean check1 = template.textContentCheck("草泥马");
        System.out.println(check1);

        String[] urls = new String[]{
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/9.jpg",
                "http://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/logo/10.jpg"
        };
        boolean check3 = template.imageContentCheck(urls);
        System.out.println(check3);


        String[] urls2 = new String[]{
                "https://tanhuasz.oss-cn-shenzhen.aliyuncs.com/2.png",
                "https://tanhuasz.oss-cn-shenzhen.aliyuncs.com/1.jpg"
        };
        boolean check4 = template.imageContentCheck(urls2);
        System.out.println(check4);
    }

    @Test
    public void testImages() {
        String[] urls = new String[]{
                "https://tanhuasz.oss-cn-shenzhen.aliyuncs.com/2.png",
                "https://tanhuasz.oss-cn-shenzhen.aliyuncs.com/1.jpg"
        };
        boolean check = template.imageContentCheck(urls);
        System.out.println(check);
    }
}