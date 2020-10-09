/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xubo.annotation.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.xubo.annotation.AnonymousAccess;

/**
 * Annotation for mapping HTTP {@code GET} requests onto specific handler
 * methods.
 * <p>
 * 支持匿名访问   GetMapping
 * 比GetMapping注解 多了一个 @AnonymousAccess
 * 自定义注解步骤
 * 1、自定义注解类
 * 2、处理注解的工具类
 * 3、后面还需要一个注解处理器类  利用反射解析注解
 *
 * 获取验证码的时候还没有登录，但凡使用到@AnonymousGetMapping这个注解的都直接给放行，不需要权限校验
 * @AnonymousAccess 在我们使用的时候，有写接口是不需要验证权限，这个时候就需要我们给接口放行，使用方式如下
 *
 *
 * @author liaojinlong
 * @see RequestMapping
 */
@AnonymousAccess
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = RequestMethod.GET)
public @interface AnonymousGetMapping {

    /**
     * Alias for {@link RequestMapping#name}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String name() default "";

    /**
     * Alias for {@link RequestMapping#value}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    /**
     * Alias for {@link RequestMapping#path}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    /**
     * Alias for {@link RequestMapping#params}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    /**
     * Alias for {@link RequestMapping#headers}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    /**
     * Alias for {@link RequestMapping#consumes}.
     *
     * @since 4.3.5
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    /**
     * Alias for {@link RequestMapping#produces}.
     */
    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};

}
