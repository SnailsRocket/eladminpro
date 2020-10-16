package com.xubo.modules.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xubo.modules.security.config.bean.LoginProperties;
import com.xubo.modules.security.config.bean.SecurityProperties;

/**
 * @Author Druid_Xu
 * @Date 2020/10/9 上午 11:46
 * @Description 这个配置类里面可以定义一个或者多个bean,Spring Boot 启动的时候会自动加载配置类和启动类,
 * @Configuration 相当于 用xml 配置Spring的 <beans>  @Bean 相当于 <bean>
 * @ConfigurationProperties 这个注解是读取 application.yml 里面的配置信息，在springboot 启动的时候回给LoginProperties 的 LoginCode赋值
 *
 */
@Configuration
public class ConfigBeanConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "login",ignoreInvalidFields = true)
    public LoginProperties loginProperties() {
        return new LoginProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "jwt",ignoreInvalidFields = true)
    public SecurityProperties securityProperties() {
        return new SecurityProperties();
    }



}
