package com.tanhua.dubbo;

import org.apache.dubbo.config.annotation.Service;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 服务提供者启动类
 */
@SpringBootApplication
@MapperScan("com.tanhua.dubbo.mapper")
public class DubboServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboServiceApplication.class,args);
    }
}
