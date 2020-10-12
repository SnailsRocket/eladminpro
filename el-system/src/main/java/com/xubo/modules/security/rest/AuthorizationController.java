package com.xubo.modules.security.rest;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wf.captcha.base.Captcha;
import com.xubo.annotation.rest.AnonymousGetMapping;
import com.xubo.modules.security.config.bean.LoginProperties;
import com.xubo.modules.security.config.bean.SecurityProperties;
import com.xubo.utils.RedisUtils;

import cn.hutool.core.util.IdUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Druid_Xu
 * @Description TODO
 * @Date 2020/10/8 下午 04:50
 *
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
//@CrossOrigin(origins = {"http://localhost:8013"})
public class AuthorizationController {
    @Resource
    public SecurityProperties securityProperties;

    @Resource
    public LoginProperties loginProperties;
    /**
     *
     *
     * @return
     */
    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
//      获取运算的结果
        Captcha captcha = loginProperties.getCaptcha();

        String uuid = securityProperties.getCodeKey() + IdUtil.simpleUUID();

//        获取 Captcha 里面的内容
        String captchaValue = captcha.text();

        RedisUtils



//

        return ResponseEntity.ok(captchaValue);
    }

}
