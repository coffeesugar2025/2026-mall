# 一，MyBatis-Plus DO自动生成MySQL DDL工具包实现
整体方案：**扫描指定包下所有DO实体类 → 解析MyBatis-Plus注解+Java字段注解 → 组装MySQL建表DDL → 写入SQL文件**
完整可运行代码，分工具类、核心解析器、实体示例、启动测试四部分，基于Java反射+MyBatis-Plus原生注解。

## 一、依赖引入（Maven）
```xml
<!-- MyBatis-Plus 核心 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3.1</version>
</dependency>
<!-- 反射工具、IO工具 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.22</version>
</dependency>
```

## 二、核心工具类：MysqlDdlGenerator
### 功能说明
1. 指定扫描包路径，扫描所有带`@TableName`的DO实体
2. 解析注解：`@TableName`、`@TableId`、`@TableField`
3. 映射Java类型 → MySQL字段类型
4. 拼接主键、注释、字段长度、非空、默认值
5. 生成完整`CREATE TABLE IF NOT EXISTS`语句
6. 输出到指定`.sql`文件，追加写入

```java
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MybatisPlus DO实体自动生成MySQL DDL生成器
 */
public class MysqlDdlGenerator {

    // 输出sql文件路径
    private final String outputSqlPath;
    // 需要扫描的DO包路径
    private final String scanPackage;
    // Java类型 -> MySQL字段类型映射
    private static final Map<String, String> JAVA_TO_MYSQL_MAP;

    static {
        JAVA_TO_MYSQL_MAP = new HashMap<>();
        // 基础数值
        JAVA_TO_MYSQL_MAP.put("Integer", "INT");
        JAVA_TO_MYSQL_MAP.put("int", "INT");
        JAVA_TO_MYSQL_MAP.put("Long", "BIGINT");
        JAVA_TO_MYSQL_MAP.put("long", "BIGINT");
        JAVA_TO_MYSQL_MAP.put("Short", "SMALLINT");
        JAVA_TO_MYSQL_MAP.put("Byte", "TINYINT");
        JAVA_TO_MYSQL_MAP.put("Float", "FLOAT");
        JAVA_TO_MYSQL_MAP.put("Double", "DOUBLE");
        JAVA_TO_MYSQL_MAP.put("BigDecimal", "DECIMAL(18,2)");
        // 字符串
        JAVA_TO_MYSQL_MAP.put("String", "VARCHAR(255)");
        // 布尔
        JAVA_TO_MYSQL_MAP.put("Boolean", "TINYINT(1)");
        JAVA_TO_MYSQL_MAP.put("boolean", "TINYINT(1)");
        // 时间日期
        JAVA_TO_MYSQL_MAP.put("LocalDateTime", "DATETIME");
        JAVA_TO_MYSQL_MAP.put("LocalDate", "DATE");
        JAVA_TO_MYSQL_MAP.put("Date", "DATETIME");
        // 大文本
        JAVA_TO_MYSQL_MAP.put("byte[]", "BLOB");
    }

    public MysqlDdlGenerator(String scanPackage, String outputSqlPath) {
        this.scanPackage = scanPackage;
        this.outputSqlPath = outputSqlPath;
        // 清空原有文件，初始化头部注释
        File file = new File(outputSqlPath);
        FileUtil.writeUtf8("# 自动生成建表SQL，生成时间：" + LocalDateTime.now() + "\n", file);
    }

    /**
     * 入口：执行生成DDL并写入文件
     */
    public void generate() throws Exception {
        // 1. 扫描包下所有Class
        Set<Class<?>> doClassList = scanDoClass(scanPackage);
        if (doClassList.isEmpty()) {
            System.out.println("未扫描到带@TableName注解的DO实体");
            return;
        }
        // 2. 遍历实体生成建表语句
        StringBuilder allSql = new StringBuilder();
        for (Class<?> clazz : doClassList) {
            String createTableSql = buildCreateTableSql(clazz);
            allSql.append(createTableSql).append("\n\n");
        }
        // 3. 追加写入文件
        FileUtil.appendUtf8(allSql.toString(), outputSqlPath);
        System.out.println("DDL生成完成，文件路径：" + new File(outputSqlPath).getAbsolutePath());
    }

    /**
     * 包扫描，获取所有带@TableName的DO类
     */
    private Set<Class<?>> scanDoClass(String basePackage) throws Exception {
        Set<Class<?>> classSet = new HashSet<>();
        String packageSearchPath = "classpath*:" + basePackage.replace(".", "/") + "/**/*.class";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(packageSearchPath);
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

        for (Resource resource : resources) {
            MetadataReader reader = readerFactory.getMetadataReader(resource);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);
            // 只处理标记@TableName的DO
            if (clazz.isAnnotationPresent(TableName.class)) {
                classSet.add(clazz);
            }
        }
        return classSet;
    }

    /**
     * 根据DO实体构建CREATE TABLE语句
     */
    private String buildCreateTableSql(Class<?> doCls) {
        // 1. 获取表名注解
        TableName tableNameAnno = doCls.getAnnotation(TableName.class);
        String tableName = tableNameAnno.value();
        String tableComment = tableNameAnno.comment();

        // 2. 拼接字段列表
        List<String> fieldSqlList = new ArrayList<>();
        String primaryKey = null;

        // 遍历所有字段（包含父类，可根据需求改成getDeclaredFields只取本类）
        Field[] fields = doCls.getFields();
        if (fields.length == 0) {
            fields = doCls.getDeclaredFields();
        }
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldSql = parseField(field);
            fieldSqlList.add(fieldSql);

            // 判断是否主键
            TableId tableIdAnno = field.getAnnotation(TableId.class);
            if (tableIdAnno != null) {
                TableField tableFieldAnno = field.getAnnotation(TableField.class);
                String columnName = StrUtil.isNotBlank(tableFieldAnno.value()) ? tableFieldAnno.value() : field.getName();
                primaryKey = columnName;
            }
        }

        // 3. 拼接建表语句
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
        // 字段行
        for (int i = 0; i < fieldSqlList.size(); i++) {
            sql.append("  ").append(fieldSqlList.get(i));
            if (i != fieldSqlList.size() - 1) {
                sql.append(",");
            }
            sql.append("\n");
        }
        // 主键约束
        if (StrUtil.isNotBlank(primaryKey)) {
            sql.append("  PRIMARY KEY (`").append(primaryKey).append("`)\n");
        }
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='").append(tableComment).append("';");
        return sql.toString();
    }

    /**
     * 解析单个字段，生成字段定义SQL
     */
    private String parseField(Field field) {
        // 字段注解
        TableField tableField = field.getAnnotation(TableField.class);
        TableId tableId = field.getAnnotation(TableId.class);

        // 数据库列名
        String columnName;
        if (tableField != null && StrUtil.isNotBlank(tableField.value())) {
            columnName = tableField.value();
        } else {
            columnName = field.getName();
        }

        // Java类型映射MySQL类型
        String javaType = field.getType().getSimpleName();
        String mysqlType = JAVA_TO_MYSQL_MAP.getOrDefault(javaType, "VARCHAR(255)");

        // 字段注释
        String comment = "";
        if (tableField != null) {
            comment = tableField.comment();
        }

        // 主键自增判断
        boolean autoIncrement = false;
        if (tableId != null && tableId.type() == IdType.AUTO) {
            autoIncrement = true;
        }

        // 拼接字段定义
        StringBuilder fieldSql = new StringBuilder("`").append(columnName).append("` ").append(mysqlType);
        // 主键自增
        if (autoIncrement) {
            fieldSql.append(" AUTO_INCREMENT");
        }
        // 注释
        if (StrUtil.isNotBlank(comment)) {
            fieldSql.append(" COMMENT '").append(comment).append("'");
        }
        return fieldSql.toString();
    }
}
```

## 三、测试启动类 GeneratorTest
```java
public class GeneratorTest {
    public static void main(String[] args) throws Exception {
        // 1. 配置扫描DO包路径
        String scanPackage = "com.example.demo.entity";
        // 2. DDL输出文件路径（项目根目录下ddl.sql）
        String sqlFilePath = "./table_ddl.sql";

        MysqlDdlGenerator generator = new MysqlDdlGenerator(scanPackage, sqlFilePath);
        generator.generate();
    }
}
```

## 四、DO实体示例（测试用）
```java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName(value = "t_user", comment = "用户信息表")
public class UserDO {

    @TableId(type = IdType.AUTO)
    @TableField(value = "id", comment = "主键ID")
    private Long id;

    @TableField(value = "user_name", comment = "用户名")
    private String userName;

    @TableField(value = "age", comment = "年龄")
    private Integer age;

    @TableField(value = "balance", comment = "账户余额")
    private BigDecimal balance;

    @TableField(value = "create_time", comment = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "is_delete", comment = "是否删除 0否1是")
    private Boolean isDelete;
}
```

## 五、生成输出示例 table_ddl.sql
```sql
# 自动生成建表SQL，生成时间：2026-06-30T15:20:30.123
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT AUTO_INCREMENT COMMENT '主键ID',
  `user_name` VARCHAR(255) COMMENT '用户名',
  `age` INT COMMENT '年龄',
  `balance` DECIMAL(18,2) COMMENT '账户余额',
  `create_time` DATETIME COMMENT '创建时间',
  `is_delete` TINYINT(1) COMMENT '是否删除 0否1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```

# 扩展增强点（可按需改造）
1. **支持父类字段**
   当前只解析本类字段，递归循环`getSuperclass()`即可解析公共父类（如BaseDO的create_time、update_time）。

2. **字段长度、非空、默认值扩展**
   自定义注解或复用`@TableField`扩展属性，增加 `NOT NULL DEFAULT 'xxx'`。

3. **分文件输出**
   每个表单独生成一个sql文件，而非全部写入同一个文件。

4. **索引自动生成**
   新增`@TableIndex`自定义注解，解析唯一索引、普通索引、联合索引并拼接`UNIQUE KEY / KEY`。

5. **兼容MyBatis-Plus逻辑删除、乐观锁**
   识别`@TableLogic`、`@Version`注解，在注释中标记说明。

6. **SpringBoot项目一键启动**
   封装成`CommandLineRunner`，项目启动自动生成DDL，无需手动运行main方法。

# 使用注意事项
1. DO实体必须添加`@TableName`注解，否则不会被扫描；
2. 字段必须添加`@TableField`才能读取字段注释、自定义列名；
3. 主键自增依赖`@TableId(type = IdType.AUTO)`；
4. 复杂类型（枚举、自定义对象）需要补充`JAVA_TO_MYSQL_MAP`映射规则；
5. 输出文件路径支持绝对路径/相对路径，使用Hutool FileUtil保证跨平台兼容。



# 二，SpringMVC 包扫描接口文档生成工具
功能：传入扫描包路径，自动扫描 `@RestController/@Controller`，解析 `@RequestMapping/@GetMapping/@PostMapping` 等注解，提取接口路径、请求方式、接口注释、入参、返回值，输出文本/文件。
基于 Spring 元数据扫描 + 反射解析，无侵入，可直接集成 SpringBoot。

## 一、Maven 依赖
```xml
<!-- Spring 核心元数据扫描 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
</dependency>
<!-- Hutool IO、工具类 -->
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.22</version>
</dependency>
<!-- 注解API，读取注释文档（可选，获取接口中文备注） -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
</dependency>
```

## 二、核心实体：接口信息存储模型
```java
import lombok.Data;
import java.util.List;

/**
 * 接口信息模型
 */
@Data
public class ApiInfo {
    // 控制器类名
    private String controllerClassName;
    // 控制器路径前缀
    private String classPrefix;
    // 控制器备注
    private String controllerDesc;
    // 接口名称
    private String apiName;
    // 请求地址
    private String url;
    // 请求方式 GET/POST/PUT/DELETE
    private List<String> httpMethod;
    // 方法注释
    private String apiDesc;
    // 方法全限定名
    private String methodFullName;
}
```

## 三、核心扫描生成工具 ApiScanner
```java
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SpringMVC Controller 接口扫描器
 * 扫描指定包，提取所有RestController/Controller接口并输出到文件
 */
public class SpringMvcApiScanner {

    // 扫描包路径
    private final String scanPackage;
    // 输出文件路径
    private final String outputFilePath;

    // 控制器注解
    private static final List<Class<? extends Annotation>> CONTROLLER_ANNOTATIONS = Arrays.asList(
            RestController.class,
            Controller.class
    );

    // 请求映射注解
    private static final Map<Class<? extends Annotation>, String> MAPPING_ANNO_MAP;

    static {
        MAPPING_ANNO_MAP = new HashMap<>();
        MAPPING_ANNO_MAP.put(RequestMapping.class, "");
        MAPPING_ANNO_MAP.put(GetMapping.class, "GET");
        MAPPING_ANNO_MAP.put(PostMapping.class, "POST");
        MAPPING_ANNO_MAP.put(PutMapping.class, "PUT");
        MAPPING_ANNO_MAP.put(DeleteMapping.class, "DELETE");
        MAPPING_ANNO_MAP.put(PatchMapping.class, "PATCH");
    }

    public SpringMvcApiScanner(String scanPackage, String outputFilePath) {
        this.scanPackage = scanPackage;
        this.outputFilePath = outputFilePath;
        // 清空文件，写入头部
        FileUtil.writeUtf8("# SpringMVC 接口清单\n# 扫描包：" + scanPackage + "\n# 生成时间：" + new Date() + "\n\n", new File(outputFilePath));
    }

    /**
     * 入口：执行扫描并输出文件
     */
    public void scanAndWrite() throws Exception {
        // 1. 扫描包下所有class
        Set<Class<?>> controllerClasses = scanControllerClass();
        if (controllerClasses.isEmpty()) {
            FileUtil.appendUtf8("未扫描到任何 @Controller / @RestController\n", outputFilePath);
            return;
        }

        List<ApiInfo> allApiList = new ArrayList<>();
        for (Class<?> clazz : controllerClasses) {
            List<ApiInfo> apiList = parseController(clazz);
            allApiList.addAll(apiList);
        }

        // 2. 格式化输出文本
        StringBuilder content = new StringBuilder();
        for (ApiInfo api : allApiList) {
            content.append("=====================================\n");
            content.append("控制器：").append(api.getControllerClassName()).append(" 【").append(api.getControllerDesc()).append("】\n");
            content.append("接口名称：").append(api.getApiName()).append("\n");
            content.append("请求地址：").append(api.getUrl()).append("\n");
            content.append("请求方式：").append(api.getHttpMethod()).append("\n");
            content.append("接口描述：").append(api.getApiDesc()).append("\n");
            content.append("方法签名：").append(api.getMethodFullName()).append("\n\n");
        }

        // 3. 写入文件
        FileUtil.appendUtf8(content.toString(), outputFilePath);
        System.out.println("接口清单生成完成，文件路径：" + new File(outputFilePath).getAbsolutePath());
    }

    /**
     * 包扫描，筛选所有Controller类
     */
    private Set<Class<?>> scanControllerClass() throws Exception {
        Set<Class<?>> classSet = new HashSet<>();
        String searchPath = "classpath*:" + scanPackage.replace(".", "/") + "/**/*.class";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(searchPath);
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

        for (Resource res : resources) {
            MetadataReader reader = readerFactory.getMetadataReader(res);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = Class.forName(className);
            // 判断是否标记控制器注解
            boolean isController = CONTROLLER_ANNOTATIONS.stream()
                    .anyMatch(clazz::isAnnotationPresent);
            if (isController) {
                classSet.add(clazz);
            }
        }
        return classSet;
    }

    /**
     * 解析单个Controller，提取所有接口方法
     */
    private List<ApiInfo> parseController(Class<?> clazz) {
        List<ApiInfo> result = new ArrayList<>();
        // 类上 @RequestMapping 前缀
        String classPrefix = "";
        RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            classPrefix = classMapping.value()[0];
        }

        // 遍历所有public方法
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            ApiInfo api = parseMethod(clazz, method, classPrefix);
            if (api != null) {
                result.add(api);
            }
        }
        return result;
    }

    /**
     * 解析单个接口方法
     */
    private ApiInfo parseMethod(Class<?> clazz, Method method, String classPrefix) {
        ApiInfo api = new ApiInfo();
        api.setControllerClassName(clazz.getSimpleName());
        api.setClassPrefix(classPrefix);
        api.setMethodFullName(clazz.getName() + "#" + method.getName());

        List<String> httpMethods = new ArrayList<>();
        String methodPath = "";
        boolean isApiMethod = false;

        // 遍历所有映射注解
        for (Map.Entry<Class<? extends Annotation>, String> entry : MAPPING_ANNO_MAP.entrySet()) {
            Class<? extends Annotation> annoCls = entry.getKey();
            Annotation anno = method.getAnnotation(annoCls);
            if (anno == null) {
                continue;
            }
            isApiMethod = true;
            String methodType = entry.getValue();

            // 反射获取注解 value 路径
            try {
                String[] paths = (String[]) anno.getClass().getMethod("value").invoke(anno);
                if (paths.length > 0) {
                    methodPath = paths[0];
                }
            } catch (Exception e) {
                // 无value属性忽略
            }

            // 处理 RequestMapping 多method
            if (anno instanceof RequestMapping) {
                RequestMapping rm = (RequestMapping) anno;
                RequestMethod[] methods = rm.method();
                if (methods.length == 0) {
                    httpMethods.add("ALL");
                } else {
                    httpMethods.addAll(Arrays.stream(methods)
                            .map(RequestMethod::name)
                            .collect(Collectors.toList()));
                }
            } else {
                httpMethods.add(methodType);
            }
        }

        // 非接口方法直接返回
        if (!isApiMethod) {
            return null;
        }

        // 拼接完整URL
        String fullUrl = joinPath(classPrefix, methodPath);
        api.setUrl(fullUrl);
        api.setHttpMethod(httpMethods);
        api.setApiName(method.getName());
        // 简易注释（可扩展读取JavaDoc注释）
        api.setApiDesc("");
        api.setControllerDesc("");
        return api;
    }

    /**
     * 拼接路径，处理斜杠
     */
    private String joinPath(String prefix, String path) {
        if (StrUtil.isBlank(prefix)) return path;
        if (StrUtil.isBlank(path)) return prefix;
        prefix = StrUtil.removeSuffix(prefix, "/");
        path = StrUtil.removePrefix(path, "/");
        return prefix + "/" + path;
    }
}
```

## 四、测试启动类
```java
public class ApiListGeneratorTest {
    public static void main(String[] args) throws Exception {
        // 需要扫描的Controller包
        String scanPackage = "com.example.demo.controller";
        // 输出文件路径
        String outputFile = "./api_list.txt";

        SpringMvcApiScanner scanner = new SpringMvcApiScanner(scanPackage, outputFile);
        scanner.scanAndWrite();
    }
}
```

## 五、Controller 示例
```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/list")
    public String getUserList() {
        return "";
    }

    @PostMapping("/add")
    public String addUser() {
        return "";
    }
}
```

## 六、输出文件 api_list.txt 示例
```
# SpringMVC 接口清单
# 扫描包：com.example.demo.controller
# 生成时间：Tue Jun 30 16:20:00 CST 2026

=====================================
控制器：UserController 【】
接口名称：getUserList
请求地址：/user/list
请求方式：[GET]
接口描述：
方法签名：com.example.demo.controller.UserController#getUserList

=====================================
控制器：UserController 【】
接口名称：addUser
请求地址：/user/add
请求方式：[POST]
接口描述：
方法签名：com.example.demo.controller.UserController#addUser
```

# 可扩展增强功能
1. **读取JavaDoc注释**
   使用 `JavaParser` 解析源码，获取接口中文说明、参数说明，填充 `apiDesc`、`controllerDesc`。

2. **解析请求参数**
   解析 `@RequestParam/@RequestBody/@PathVariable`，提取参数名、类型、是否必填。

3. **导出 Markdown / Excel**
   改造输出格式，生成标准接口文档md，或导出csv表格。

4. **SpringBoot 自动启动**
   实现 `CommandLineRunner`，项目启动时自动生成接口清单，无需手动运行main。

5. **过滤父类/工具方法**
   过滤 `Object` 继承方法、私有方法、无映射注解的普通工具方法。

6. **支持 @Api Swagger 注解兼容**
   读取 `@Api`、`@ApiOperation` 填充接口中文名称与描述。

7. **去重、排序**
   按控制器分组、URL升序输出，重复接口自动合并。

# 使用说明
1. 传入项目 Controller 存放包路径；
2. 自动识别 `@RestController` / `@Controller`；
3. 兼容 `@GetMapping/@PostMapping/@RequestMapping` 等所有标准SpringMVC路由注解；
4. 自动拼接类路径前缀 + 方法路径，统一处理斜杠；
5. 支持多请求方式（RequestMapping指定多个Method）；
6. 输出纯文本文件，可直接复制到文档。