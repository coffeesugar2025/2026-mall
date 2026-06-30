package com.youlai.boot;

import com.youlai.boot.generate.MysqlDdlGenerator;
import com.youlai.boot.generate.SpringMvcApiScanner;
import org.junit.jupiter.api.Test;

public class ExportTest {


    public static void main(String[] args) throws Exception {
        //testGenerateAPI();

        testGenerateSQL();
    }

    public static void testGenerateAPI() throws Exception {
        // 1. 配置扫描DO包路径
        String scanPackage = "com.youlai.boot";
        // 2. DDL输出文件路径（项目根目录下ddl.sql）
        String sqlFilePath = "d:\\table_API.txt";
        try {
            SpringMvcApiScanner generator = new SpringMvcApiScanner(scanPackage, sqlFilePath);
            generator.scanAndWrite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void testGenerateSQL() throws Exception {
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
