/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xubo.modules.security.service.dto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xubo.modules.system.service.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Druid
 * @date 2020-9-21
 *  DTO 类 extends UserDetails(这个类是SpringSecurity core 提供的)
 * @JsonIgnore 类注解
 * 放在属性和方法上面 ， 表示 生成JSON的时候，忽略该属性和方法(序列化和反序列化也包括Object转换成JSON)
 * 对象只有继承 Serializable 接口的对象才能被序列化
 * sh 致宇
 */
@Getter
@AllArgsConstructor
public class JwtUserDto implements UserDetails {

    private final UserDto user;

    private final List<Long> dataScopes;

    @JsonIgnore
    private final List<GrantedAuthority> authorities;

    public Set<String> getRoles() {
//        JDK 1.8 的新写法  函数式编程 这种在外面用的挺多的
//        下面这段代码的意思是 获取authorities 这个List集合中GrantedAuthority 对象的 getAuthority 属性 ，并放入到Set<String>集合中
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
