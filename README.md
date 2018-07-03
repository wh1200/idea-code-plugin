## Introduction

The contents following are written in Chinese and translated by google translate.

This project is an Intelli IDEAJ plugin
Initially created for the company's unified programming specification, it mainly submits some code checks that IDE failed to provide, as well as some convenient development features. It also includes support for Java and Kotlin, as well as support for the front-end framework VueJS.

The functions contained in the project may be somewhat complicated and cannot be reflected in the project name, so a name is casually used.

This project was originally used for internal use by the company. The main purpose of open source is to let everyone provide some questions for inspection and suggestions.


## Features

### Mandatory style
- Some mandatory presets for code styles at project startup, including indentation and encoding
- Forced code rearrangement
- Write some file templates by default (Kotlin)

### Encoding Check
- Check the file encoding is UTF-8 or not.

### Indent check
- Indent check for checking code style configuration of IDE, included languages are java, kotlin, javascript, 
typescript, vue etc.
- Normal indentation is set to 2 spaces, and continuous indentation is twice as long as normal indentation. It is 4 
spaces.

### Java & Kotlin related
- The line count of a .java file or .kt file is limited to **800** lines.
- The line count of java method or kotlin function is limited to **100** lines.
- The class must add a document comment with @author and @since tags.
- The interface method must add a document comment.
- Direct use of numbers as arguments is forbidden(check for numbers great than 10), provided code correction function can extract parameters as variables
- Added space check and fix
- Increase the sorting of fields and methods of java classes and blank line corrections
- Completely new kotlin code rearrangement function.
- Options for organizing import and code rearrangement when the formatting code is turned on by default when starting the project (Java language) v1.2.6
- Except for the JUnit test class, it is not allowed to use "System.out.println" or "System.err.println" or "println"
 in kotlin for console output. The output should always uses the log and provides the repair function. The default is
  to use slf4j.
- Add naming pattern check for property, method, field, function and constant property.
- Clear blank lines at start and end of companion object.
- Val property in object should be named like a const property.
 
### Spring Framework Related
- When using the "@Value" annotation in a class to import spring's environment variables, code hints are given for 
the contents of spring boot's default configuration file application.yml (temporarily not supporting properties file).
- Use the "@Value" annotation in the class to introduce spring's environment variable, you can use the find statement to jump to the corresponding configuration item in application.yml

### Front End Related
- When the .vue file is formatted, the label attributes in the template are reordered, one line per attribute.
- When the .vue file is formatted, the label property in the template starts with v- or: and the property value is 
formatted.
- The length of the template part (template tag part) in the .vue file must not exceed 150 lines.
- .vue file template, if the property value is a complex expression, can be extracted as a calculated property.


1.3.8 更新内容 

1. 增加接口格式化代码补全注释
   对于接口类，格式化代码时如果方法没有注释则会根据参数和返回值自动生成模板注释，但注释内的文字说明需自行补充
1. 增加属性名称类的生成
   
   对于带有Entity、Table或Documented注解的类，生成一个以属性名称为成员变量的类（Kotlin在同一个文件中生成，Java在同一个包下面生成），目的是在进行查询（不管是es、mongodb还是关系型数据库）时，对属性名称的引用使用变量而不是直接的字符串常量，这样既可以避免不必要的拼写错误，更重要的是在重构属性名称的时候可以很方便的一次性修改
1. mybatis的mapper接口类与对应xml文件：
   1. 从mapper接口类的方法前生成导航图标可以跳转到xml对应的sql定义
   2. 从xml的sql定义可以通过前面的图标跳转到对应的mapper接口方法
   3. 从sql模板引用（include标签）的refid可以链接到对应的sql模板定义
   4. 从sql模板定义（sql标签）可以直接跳转到引用模板的地方
   5. 可以不用mybatis插件（要收费，比较贵）
   

## Build

Copy the project code to the local, open the project using idea, configure the IntelliJ Platform Plugin SDK, and add the following in the Classpath configuration of the SDK:/Applications/IntelliJ IDEA.app/Contents/plugins/JavaScriptLanguage/lib/javascript-openapi.jar
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

Then select Build - Prepare Plugin Module For Deployment. After the build is completed, a zip file will be generated in the project directory. The file is the installation file for the idea plug-in.

> Note that the VueJS plugin is located in the user plugin installation directory instead of the IDEA installation 
directory. For Mac users, the plugin directory is in path of "~/Library/Application Support/Intellij IDEA/".
