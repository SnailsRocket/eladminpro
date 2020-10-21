package com.xubo.modules.security.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wf.captcha.base.Captcha;
import com.xubo.annotation.Log;
import com.xubo.annotation.rest.AnonymousDeleteMapping;
import com.xubo.annotation.rest.AnonymousGetMapping;
import com.xubo.annotation.rest.AnonymousPostMapping;
import com.xubo.config.RsaProperties;
import com.xubo.exception.BadRequestException;
import com.xubo.modules.security.config.bean.LoginCodeEnum;
import com.xubo.modules.security.config.bean.LoginProperties;
import com.xubo.modules.security.config.bean.SecurityProperties;
import com.xubo.modules.security.security.TokenProvider;
import com.xubo.modules.security.service.OnlineUserService;
import com.xubo.modules.security.service.dto.AuthUserDto;
import com.xubo.modules.security.service.dto.JwtUserDto;
import com.xubo.utils.RedisUtils;
import com.xubo.utils.RsaUtils;
import com.xubo.utils.StringUtils;
import cn.hutool.core.util.IdUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Druid_Xu
 * @Date 2020/10/8 下午 04:50
 * @Description
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
//@CrossOrigin(origins = {"http://localhost:8013"})
public class AuthorizationController {
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final TokenProvider tokenProvider;

    @Resource
    private final OnlineUserService onlineUserService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

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
//        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        redisUtils.set(uuid, captchaValue, 3, TimeUnit.MINUTES);

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

    /**
     * @Log 是 el-logging 模块里面自定义的注解
     * @param
     * @return
     * 194
     * 12  5
     *
     * @Validated 是校验 入参 是否满足条件 ，还要在 AuthUserDto 这个Bean里面设置校验规则(NotBlank)
     *
     */
    @Log("用户登录")
    @ApiOperation("登录授权")
    @AnonymousPostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated@RequestBody AuthUserDto authUserDto, HttpServletRequest request) throws Exception {
        /*String header = request.getHeader("User-Agent");
        Enumeration<String> headerNames = request.getHeaderNames();
        System.out.println("---------------");
        while(headerNames.hasMoreElements()) {
            System.out.println(headerNames.nextElement());
        }
        System.out.println("---------------");
        System.out.println(header);
        UserAgent userAgent = UserAgent.parseUserAgentString(header);
        // 获取客户端操作系统
        OperatingSystem operatingSystem = userAgent.getOperatingSystem();
        // 获取客户端浏览器
        Browser browser = userAgent.getBrowser();

        System.out.println("operatingSyetem-------------");
        System.out.println(operatingSystem.name());
        System.out.println(operatingSystem.getName());
        System.out.println(operatingSystem.getId());
        System.out.println(operatingSystem.getDeviceType());
        System.out.println(operatingSystem.getGroup());
        System.out.println(operatingSystem.getManufacturer());

        System.out.println("brower---------------------");
        System.out.println(browser.getId());
        System.out.println(browser.getGroup());
        System.out.println(browser.getName());
        System.out.println(browser.getVersion("User-Agent"));
        System.out.println(browser.getRenderingEngine());
*/
        // 解密AuthUserDto 中的密码
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUserDto.getPassword());
        // 从Redis里面获取验证码
        Object codeObject = redisUtils.get(authUserDto.getUuid());
        String code = String.valueOf(codeObject);
        // 清楚验证码
        redisUtils.del(authUserDto.getUuid());
        // 验证码有两种状态 一种是过期 一种是 验证错误，或者没有填写
        if(StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if(StringUtils.isBlank(authUserDto.getCode()) || !authUserDto.getCode().equals(code)) {
            throw new BadRequestException("验证码错误");
        }
        // username，password封装到 UsernamePasswordAuthenticationToken 对象中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUserDto.getUsername(), password);
        //TODO bug 反射没找到method  当前登录状态已过期，请重新登录
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 获取生成的token
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto)authentication.getPrincipal();
        // 保存用户在线信息，并且设置过期时间
        onlineUserService.save(jwtUserDto, token, request);
        // 返回token和用户信息 ，封装到map里面
        Map<String, Object> authInfo = new HashMap<String, Object>(2){{
            put("token", properties.getTokenStartWith() + token);
            put("user", jwtUserDto);
        }};
        if(loginProperties.isSingleLogin()) {
            // 踢掉之前已经登录的 token
            onlineUserService.checkLoginOnUser(authUserDto.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    /**
     * HttpStatus 这个也是Spring - web 提供的状态码类
     *
     * @param request
     * @return
     */
    @ApiOperation("退出登录")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
