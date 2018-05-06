## 简介

本项目是一个Intelli IDEAJ插件
最初是为公司统一编程规范创建的，主要提交一些ide未能提供的代码检查，以及一些方便开发的功能，同时包含对Java以及Kotlin的支持，以及前端框架VueJS的支持

项目包含的功能可能有些杂，并不能在项目名称中体现出来，所以随便起了一个名字

本项目原本用于公司内部使用，开源的主要目的是想让大家能够提供一些问题检查和建议

该项目中的文字提示信息均采用了中文

## 功能介绍

### 编码检查
- 检查文件的编码如果不是UTF-8时，进行错误提示

### 缩进检查
- 缩减检查为检查ide的code style配置，检查的范围为java、kotlin、javascript、typescript、vue

### Java&Kotlin相关
- java文件或kt文件的长度不能超过 800 行，否则提示错误
- 类方法不得超过 100 行，否则提示错误
- 类必须添加注释，否则提示错误
- 接口方法必须添加注释，否则提示错误
- 直接使用数字作为参数提示错误，提供的代码修正功能可以将参数提取为变量
- 增加了空格检查以及修复

### Spring相关
- 在类中使用@Value注解引入spring的环境变量时，对spring boot默认配置文件application.yml(暂不支持properties文件)的内容作了代码提示
- 在类中使用@Value注解引入spring的环境变量，可以使用查找声明的方式跳转到application.yml中对应的配置项

### 前端相关
- .vue文件在格式化的时候，模板中的标签属性会重新进行排序，且每个属性占一行
- .vue文件在格式化的时候，模板中的标签属性以v-或:开头，对属性值进行格式化
- .vue文件中模板部分（template标签部分）的长度不得超过150行，超过时请进行组件拆分


## 构建

复制项目代码到本地，使用idea打开项目，配置IntelliJ Platform Plugin SDK，并在SDK的Classpath配置中增加以下内容：
/Applications/IntelliJ IDEA.app/Contents/plugins/JavaScriptLanguage/lib/javascript-openapi.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/JavaScriptLanguage/lib/JavaScriptLanguage.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/Kotlin/lib/kotlin-plugin.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/Spring/lib/spring.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringSecurity/lib/SpringSecurity.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringMvc/lib/spring-mvc-api.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringMvc/lib/SpringMvc.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot-cloud.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot-config-yaml.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot-initializr.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot-mvc.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/SpringBoot/lib/spring-boot-run.jar
/Users/wuhao/Library/Application Support/IntelliJIdea2018.1/vuejs/lib/vuejs.jar
/Applications/IntelliJ IDEA.app/Contents/plugins/yaml/lib/yaml.jar
然后选择 Build - Prepare Plugin Module For Deployment，构建完成之后在项目目录下会生成zip文件，该文件即为idea插件的安装文件

