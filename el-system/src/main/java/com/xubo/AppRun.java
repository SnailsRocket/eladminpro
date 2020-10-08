package com.xubo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.annotations.Api;

/**
 * @Author Druid_Xu
 * @Description  启动类
 * @Date 2020/10/8 下午 04:38
 *
 */
@Api(hidden = true)
@SpringBootApplication
public class AppRun {

    public static void main(String[] args) {
        SpringApplication.run(AppRun.class,args);
    }

}
