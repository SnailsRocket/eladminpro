package com.xubo.modules.security.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wf.captcha.base.Captcha;
import com.xubo.annotation.rest.AnonymousGetMapping;
import com.xubo.modules.security.config.bean.LoginCodeEnum;
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
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
//@CrossOrigin(origins = {"http://localhost:8013"})
public class AuthorizationController {
    private final RedisUtils redisUtils;

    @Resource
    public SecurityProperties securityProperties;

    @Resource
    public LoginProperties loginProperties;

    /**
     * @return
     */
    @ApiOperation("获取验证码")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
//      获取运算的结果，调用getCaptcha()这个方法的时候，才回去生成 Captcha对象，loginProperties初始化的时候只有三个属性有值
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = securityProperties.getCodeKey() + IdUtil.simpleUUID();
//        获取 Captcha 里面的内容
        String captchaValue = captcha.text();

        if (captcha.getCharType() - 1 == LoginCodeEnum.arithmetic.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
//      验证码的信息保存到redis里面 以String类型存储 第一个是key value timetype time  把 key(String) value(instanceof String) 序列化成byte[] 存入 redis
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);

//        封装到map里面的是 captcha 这个 对象而不是返回计算后的结果
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {
            {
                put("uuid",uuid);
                put("img",captcha.toBase64());
            }
        };
//        返回参数类型在ResponseEntity 里面定义的是泛型T，
        return ResponseEntity.ok(imgResult);
    }

}
