package com.tanhua.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 消费者启动类
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableCaching //开启spring cache功能
public class TanhuaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanhuaServerApplication.class,args);
    }
}
