package com.youlai.boot.generate;

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
