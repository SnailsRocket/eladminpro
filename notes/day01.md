## day01
#### bug1 
> 项目启动失败
> 定位问题： el-system 模块 security/config/bean 里面的Bean 没有注入到容器里面，所以使用不了
>exception ： 
>Description: A component required a bean of type 'com.xubo.modules.security.config.bean.LoginProperties' that could not be found.
>Action:  Consider defining a bean of type 'com.xubo.modules.security.config.bean.LoginProperties' in your configuration.
>