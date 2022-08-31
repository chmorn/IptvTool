package com.chmorn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chmorn
 * @className IptvToolApplication
 * @description 启动类
 * @date 2022/8/31
 **/
@SpringBootApplication
@EnableScheduling
public class IptvToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(IptvToolApplication.class, args);
    }

}
