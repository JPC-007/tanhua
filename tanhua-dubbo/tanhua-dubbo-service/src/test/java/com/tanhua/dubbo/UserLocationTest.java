package com.tanhua.dubbo;

import com.tanhua.dubbo.api.mongo.LocationApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserLocationTest {

    @Autowired
    private LocationApi userLocationApi;

    @Test
    public void saveLocation(){
        userLocationApi.saveLocation(1l,113.929778,22.582111,"深圳黑马程序员");
        userLocationApi.saveLocation(2l,113.925528,22.587995,"红荔村肠粉");
        userLocationApi.saveLocation(3l,113.93814,22.562578,"深圳南头直升机场");
        userLocationApi.saveLocation(4l,114.064478,22.549528,"深圳市政府");
        userLocationApi.saveLocation(5l,113.986074,22.547726,"欢乐谷");
        userLocationApi.saveLocation(6l,113.979399,22.540746,"世界之窗");
        userLocationApi.saveLocation(7l,114.294924,22.632275,"东部华侨城");
        userLocationApi.saveLocation(8l,114.314011,22.598196,"大梅沙海滨公园");
        userLocationApi.saveLocation(9l,113.821705,22.638172,"深圳宝安国际机场");
        userLocationApi.saveLocation(10l,113.912386,22.566223,"海雅缤纷城(宝安店)");
    }
}