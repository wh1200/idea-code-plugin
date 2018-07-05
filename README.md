## Introduction

The contents following are written in Chinese and translated by google translate.

This project is an Intelli IDEAJ plugin
Initially created for the company's unified programming specification, it mainly submits some code checks that IDE failed to provide, as well as some convenient development features. It also includes support for Java and Kotlin, as well as support for the front-end framework VueJS.

The functions contained in the project may be somewhat complicated and cannot be reflected in the project name, so a name is casually used.

This project was originally used for internal use by the company. The main purpose of open source is to let everyone provide some questions for inspection and suggestions.


## Features

### 根据模板创建项目

安装插件后，在设置中填写git private token，以及模板项目的地址（默认为本公司项目模板）

使用IDEA菜单 File - New - 擎盾JavaKotlin项目  和  File - New - 擎盾VueJS项目 分别创建基于项目模板的 Java&Kotlin 和 VueJS 新项目

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
