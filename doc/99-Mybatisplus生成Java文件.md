# Mybatisplus生成Java文件

pom文件

```
<dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis‑plus‑boot‑starter</artifactId>
            <version>3.5.5</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
```



1、Java逻辑

```
package com.example.demo;

import java.io.*;
import java.sql.*;
import java.util.*;

public class MyBatisPlusCodeGenerator {

    /**
     * 驼峰转换 user_name → userName
     */
    public static String toCamelCase(String str) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 表名转大驼峰类名 t_user_info → UserInfo
     */
    public static String tableToClassName(String tableName) {
        String name = tableName;
        if(name.startsWith("t_")){
            name = name.substring(2);
        }
        String camel = toCamelCase(name);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    /**
     * jdbc类型映射Java类型
     */
    public static String jdbcToJavaType(int jdbcTypeCode) {
        switch (jdbcTypeCode) {
            case Types.BIGINT: return "Long";
            case Types.INTEGER: return "Integer";
            case Types.SMALLINT: return "Integer";
            case Types.TINYINT: return "Integer";
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR: return "String";
            case Types.DATE: return "java.time.LocalDate";
            case Types.TIMESTAMP: return "java.time.LocalDateTime";
            case Types.TIME: return "java.time.LocalTime";
            case Types.DECIMAL:
            case Types.NUMERIC: return "java.math.BigDecimal";
            case Types.DOUBLE: return "Double";
            case Types.FLOAT: return "Float";
            case Types.BOOLEAN: return "Boolean";
            default: return "String";
        }
    }

    public static String getJdbcTypeName(int jdbcTypeCode) {
        return JDBCType.valueOf(jdbcTypeCode).getName();
    }

    /**
     * 获取数据库所有表元数据
     */
    public static List<TableInfo> readTableMeta(String jdbcUrl, String user, String password) throws SQLException {
        List<TableInfo> tableList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            ResultSet tablesRs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"});
            while (tablesRs.next()) {
                TableInfo table = new TableInfo();
                String tableName = tablesRs.getString("TABLE_NAME");
                table.setTableName(tableName);
                table.setClassName(tableToClassName(tableName));
                table.setTableComment(tablesRs.getString("REMARKS"));

                List<ColumnInfo> columnList = new ArrayList<>();
                ResultSet colsRs = metaData.getColumns(catalog, null, tableName, "%");
                ColumnInfo pkCol = null;
                while (colsRs.next()) {
                    ColumnInfo col = new ColumnInfo();
                    String colName = colsRs.getString("COLUMN_NAME");
                    col.setColumnName(colName);
                    col.setPropertyName(toCamelCase(colName));
                    int jdbcCode = colsRs.getInt("DATA_TYPE");
                    col.setJavaType(jdbcToJavaType(jdbcCode));
                    col.setJdbcType(getJdbcTypeName(jdbcCode));
                    col.setComment(colsRs.getString("REMARKS"));
                    String isAuto = colsRs.getString("IS_AUTOINCREMENT");
                    col.setAutoIncrement("YES".equals(isAuto));
                    columnList.add(col);
                }
                colsRs.close();

                ResultSet pkRs = metaData.getPrimaryKeys(catalog, null, tableName);
                if (pkRs.next()) {
                    String pkColumnName = pkRs.getString("COLUMN_NAME");
                    for (ColumnInfo c : columnList) {
                        if(c.getColumnName().equals(pkColumnName)){
                            c.setPrimary(true);
                            pkCol = c;
                            break;
                        }
                    }
                }
                pkRs.close();

                table.setColumns(columnList);
                table.setPrimaryKey(pkCol);
                tableList.add(table);
            }
            tablesRs.close();
        }
        return tableList;
    }

    public static void main(String[] args) throws Exception {
        // ========== 配置区 ==========
        String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "123456";
        String basePackage = "com.example.demo";
        String outputDir = "./gen-code";

        List<TableInfo> tables = readTableMeta(jdbcUrl, username, password);

        for (TableInfo table : tables) {
            Map<String, Object> model = new HashMap<>();
            model.put("table", table);
            model.put("basePackage", basePackage);
            String entityPackage = basePackage + ".dto";
            String mapperPackage = basePackage + ".mapper";
            model.put("entityPackage", entityPackage);
            model.put("mapperPackage", mapperPackage);

            String className = table.getClassName();

            // 1.生成DTO实体（带MP注解 @TableName @TableId）
            File dtoFile = new File(outputDir + "/" + className + ".java");
            writeFile(dtoFile, renderDtoTemplate(model));

            // 2.生成Mapper接口，继承 BaseMapper<T>
            File mapperJava = new File(outputDir + "/" + className + "Mapper.java");
            writeFile(mapperJava, renderMapperInterface(model));

            // ===== 不再生成 Mapper.xml =====
        }
        System.out.println("MyBatis‑Plus代码生成完成，输出目录：" + new File(outputDir).getAbsolutePath());
    }

    public static void writeFile(File file, String content) throws IOException {
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
            pw.write(content);
        }
    }

    // DTO模板：增加 @TableName、@TableId MP注解
    public static String renderDtoTemplate(Map<String,Object> m){
        TableInfo table = (TableInfo)m.get("table");
        String pkg = (String) m.get("entityPackage");
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(pkg).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.annotation.IdType;\n");
        sb.append("import com.baomidou.mybatisplus.annotation.TableId;\n");
        sb.append("import com.baomidou.mybatisplus.annotation.TableName;\n");
        sb.append("import lombok.Data;\n");

        Set<String> imports = new LinkedHashSet<>();
        for(ColumnInfo col : table.getColumns()){
            String jt = col.getJavaType();
            if(jt.contains(".")){
                imports.add(jt);
            }
        }
        for(String im : imports){
            sb.append("import ").append(im).append(";\n");
        }
        sb.append("\n/**\n");
        sb.append(" * ").append(table.getTableName()).append(" ").append(table.getTableComment()==null?"":table.getTableComment()).append("\n");
        sb.append(" */\n");
        sb.append("@Data\n");
        sb.append("@TableName(value = \"").append(table.getTableName()).append("\")\n");
        sb.append("public class ").append(table.getClassName()).append(" {\n\n");

        for(ColumnInfo col : table.getColumns()){
            sb.append("    /** ").append(col.getComment()==null?"":col.getComment()).append(" */\n");
            if(col.isPrimary()){
                if(col.isAutoIncrement()){
                    sb.append("    @TableId(type = IdType.AUTO)\n");
                }else{
                    sb.append("    @TableId\n");
                }
            }
            sb.append("    private ").append(simpleType(col.getJavaType())).append(" ").append(col.getPropertyName()).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String simpleType(String fullType){
        int idx = fullType.lastIndexOf(".");
        if(idx>0) return fullType.substring(idx+1);
        return fullType;
    }

    // Mapper接口模板：继承 BaseMapper<T>，删除手写CRUD
    public static String renderMapperInterface(Map<String,Object> m){
        TableInfo table = (TableInfo)m.get("table");
        String mapperPkg = (String)m.get("mapperPackage");
        String dtoPkg = (String)m.get("entityPackage");
        String cls = table.getClassName();
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(mapperPkg).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n");
        sb.append("import org.apache.ibatis.annotations.Mapper;\n");
        sb.append("import ").append(dtoPkg).append(".").append(cls).append(";\n\n");
        sb.append("@Mapper\n");
        sb.append("public interface ").append(cls).append("Mapper extends BaseMapper<").append(cls).append("> {\n\n");
        // 自定义SQL注解方式写在这里，使用@Select/@Update注解
        // example: @Select("select * from xxx")
        // List<User> selectCustom();

        sb.append("}\n");
        return sb.toString();
    }

    // ---------------------- 元数据POJO ----------------------
    public static class TableInfo {
        private String tableName;
        private String className;
        private String tableComment;
        private List<ColumnInfo> columns;
        private ColumnInfo primaryKey;

        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getTableComment() { return tableComment; }
        public void setTableComment(String tableComment) { this.tableComment = tableComment; }
        public List<ColumnInfo> getColumns() { return columns; }
        public void setColumns(List<ColumnInfo> columns) { this.columns = columns; }
        public ColumnInfo getPrimaryKey() { return primaryKey; }
        public void setPrimaryKey(ColumnInfo primaryKey) { this.primaryKey = primaryKey; }
    }

    public static class ColumnInfo {
        private String columnName;
        private String propertyName;
        private String javaType;
        private String jdbcType;
        private String comment;
        private boolean primary;
        private boolean autoIncrement;

        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        public String getPropertyName() { return propertyName; }
        public void setPropertyName(String propertyName) { this.propertyName = propertyName; }
        public String getJavaType() { return javaType; }
        public void setJavaType(String javaType) { this.javaType = javaType; }
        public String getJdbcType() { return jdbcType; }
        public void setJdbcType(String jdbcType) { this.jdbcType = jdbcType; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        public boolean isPrimary() { return primary; }
        public void setPrimary(boolean primary) { this.primary = primary; }
        public boolean isAutoIncrement() { return autoIncrement; }
        public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }
    }
}

```

