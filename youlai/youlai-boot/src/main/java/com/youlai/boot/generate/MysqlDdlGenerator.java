package com.youlai.boot.generate;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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
        //FileUtil.writeString("# 自动生成建表SQL，生成时间：" + LocalDateTime.now() + "\n", file);
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
        FileUtil.writeString(allSql.toString(), outputSqlPath, StandardCharsets.UTF_8);
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
        String tableComment = tableNameAnno.value();

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
            comment = tableField.value();
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

    @Test
    public void testGenerate() throws Exception {
        // 1. 配置扫描DO包路径
        String scanPackage = "com.youlai.boot";
        // 2. DDL输出文件路径（项目根目录下ddl.sql）
        String sqlFilePath = "d:\\table_ddl.sql";
        try {
            MysqlDdlGenerator generator = new MysqlDdlGenerator(scanPackage, sqlFilePath);
            generator.generate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
