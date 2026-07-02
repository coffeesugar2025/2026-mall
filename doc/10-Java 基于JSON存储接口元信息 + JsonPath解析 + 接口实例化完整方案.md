# Java 基于JSON存储接口元信息 + JsonPath解析 + 接口实例化完整方案
## 一、整体设计思路
1. **用JSON统一存储接口全部元数据**：接口地址、请求头、Query参数、Body入参定义（字段名、类型、是否必填、默认值等）
2. 元数据JSON结构分层：
   - 基础信息：`url`、`method`、`headers`
   - URL参数：`queryParams`（路径拼接参数）
   - 请求体参数定义：`bodyFields`（每个字段包含`fieldName`、`type`、`required`、`defaultVal`）
3. 核心工具：
   - Jackson：JSON序列化/反序列化，把元JSON转Java实体
   - JsonPath：动态读取/填充JSON对象、校验参数类型、取值赋值
4. 实例化流程：
   1. 加载接口定义JSON → 转为Java `ApiDefinition` 实体
   2. 构建空请求JSON对象（JsonPath的`DocumentContext`）
   3. 根据`bodyFields`遍历，外部传入业务值，按类型填充到请求JSON
   4. 拼接Query参数、组装Header、完整请求报文

## 二、依赖引入（Maven）
```xml
<!-- Jackson JSON解析 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
<!-- JsonPath 动态操作JSON -->
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.8.0</version>
</dependency>
```

## 三、接口定义JSON模板（存储的元数据）
```json
{
  "url": "https://xxx.com/api/user/add",
  "method": "POST",
  "headers": {
    "Content-Type": "application/json",
    "token": "${token}"
  },
  "queryParams": [
    {
      "fieldName": "timestamp",
      "type": "Long",
      "required": true
    }
  ],
  "bodyFields": [
    {
      "fieldName": "name",
      "type": "String",
      "required": true,
      "defaultVal": ""
    },
    {
      "fieldName": "age",
      "type": "Integer",
      "required": false,
      "defaultVal": 18
    },
    {
      "fieldName": "userInfo.phone",
      "type": "String",
      "required": false
    }
  ]
}
```
- `fieldName` 支持嵌套路径（`userInfo.phone`），正好适配JsonPath表达式
- `type` 限定基础类型：String/Integer/Long/Double/Boolean

## 四、Java实体映射（元数据载体）
### 1. 顶层接口定义实体 ApiDefinition
```java
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ApiDefinition {
    // 接口基础信息
    private String url;
    private String method;
    private Map<String, String> headers;
    // url查询参数
    private List<ParamMeta> queryParams;
    // 请求体入参定义
    private List<ParamMeta> bodyFields;
}
```

### 2. 参数元数据实体 ParamMeta
```java
import lombok.Data;

@Data
public class ParamMeta {
    // 参数字段名，支持嵌套 a.b.c
    private String fieldName;
    // 字段类型 String/Integer/Long/Double/Boolean
    private String type;
    // 是否必填
    private Boolean required;
    // 默认值
    private Object defaultVal;
}
```

## 五、核心工具类：接口实例化 + JsonPath填充
### 功能说明
1. 将接口定义JSON转为`ApiDefinition`
2. 根据外部传入参数Map，自动按类型填充请求JSON（JsonPath操作）
3. 校验必填参数缺失抛出异常
4. 拼接完整请求URL（携带query参数）
5. 输出最终请求JSON报文

```java
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ApiRequestBuilder {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 1. 加载JSON配置，转为接口定义实体
    public static ApiDefinition loadApiDef(String apiJsonStr) throws Exception {
        return OBJECT_MAPPER.readValue(apiJsonStr, ApiDefinition.class);
    }

    // 2. 实例化完整接口请求：返回 完整url、headerMap、请求体json字符串
    public static ApiRequest buildRequest(ApiDefinition apiDef, Map<String, Object> inputParamMap) throws Exception {
        // 初始化空JSON对象用于存放body
        DocumentContext jsonCtx = JsonPath.parse("{}");

        // 填充body参数
        fillBodyParams(apiDef.getBodyFields(), inputParamMap, jsonCtx);

        // 拼接query参数，生成完整访问url
        String fullUrl = buildFullUrl(apiDef.getUrl(), apiDef.getQueryParams(), inputParamMap);

        // 处理header变量替换（如${token}）
        Map<String, String> finalHeaders = resolveHeaderVars(apiDef.getHeaders(), inputParamMap);

        // 转为最终请求体字符串
        String requestBody = jsonCtx.jsonString();

        return new ApiRequest(fullUrl, apiDef.getMethod(), finalHeaders, requestBody);
    }

    // 填充请求体所有字段，JsonPath自动处理嵌套层级
    private static void fillBodyParams(List<ParamMeta> bodyFields, Map<String, Object> inputMap, DocumentContext ctx) {
        for (ParamMeta meta : bodyFields) {
            String fieldPath = meta.getFieldName();
            Object inputVal = inputMap.get(fieldPath);

            // 必填校验
            if (meta.getRequired() && inputVal == null) {
                throw new RuntimeException("参数[" + fieldPath + "]不能为空");
            }

            // 无传入值使用默认值
            Object finalVal = inputVal == null ? meta.getDefaultVal() : convertType(inputVal, meta.getType());

            // JsonPath设置值，支持嵌套 a.b.c
            ctx.set("$." + fieldPath, finalVal);
        }
    }

    // 类型转换，统一转成JSON兼容类型
    private static Object convertType(Object val, String targetType) {
        if (val == null) return null;
        return switch (targetType.trim()) {
            case "Integer" -> Integer.valueOf(val.toString());
            case "Long" -> Long.valueOf(val.toString());
            case "Double" -> Double.valueOf(val.toString());
            case "Boolean" -> Boolean.valueOf(val.toString());
            default -> val.toString();
        };
    }

    // 拼接URL + query参数
    private static String buildFullUrl(String baseUrl, List<ParamMeta> queryParams, Map<String, Object> inputMap) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseUrl;
        }
        List<String> queryList = new ArrayList<>();
        for (ParamMeta meta : queryParams) {
            String key = meta.getFieldName();
            Object val = inputMap.get(key);
            if (meta.getRequired() && val == null) {
                throw new RuntimeException("URL参数[" + key + "]必填");
            }
            if (val != null) {
                queryList.add(key + "=" + val);
            }
        }
        String queryStr = String.join("&", queryList);
        return baseUrl + (baseUrl.contains("?") ? "&" : "?") + queryStr;
    }

    // 替换Header中 ${xxx} 占位符
    private static Map<String, String> resolveHeaderVars(Map<String, String> originHeaders, Map<String, Object> inputMap) {
        if (originHeaders == null) return new HashMap<>();
        return originHeaders.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> replaceVar(entry.getValue(), inputMap)
                ));
    }

    private static String replaceVar(String content, Map<String, Object> inputMap) {
        if (!content.contains("${")) return content;
        int start = content.indexOf("${");
        int end = content.indexOf("}", start);
        if (start == -1 || end == -1) return content;
        String varKey = content.substring(start + 2, end);
        Object varVal = inputMap.get(varKey);
        String replaceVal = varVal == null ? "" : varVal.toString();
        return content.substring(0, start) + replaceVal + content.substring(end + 1);
    }

    // 请求结果封装实体
    @Data
    public static class ApiRequest {
        private String fullUrl;
        private String method;
        private Map<String, String> headers;
        private String requestBody;

        public ApiRequest(String fullUrl, String method, Map<String, String> headers, String requestBody) {
            this.fullUrl = fullUrl;
            this.method = method;
            this.headers = headers;
            this.requestBody = requestBody;
        }
    }
}
```

## 六、调用示例（接口实例化完整流程）
```java
public class TestDemo {
    public static void main(String[] args) throws Exception {
        // 1. 存储在数据库/文件中的接口定义JSON
        String apiJson = """
                {
                  "url": "https://xxx.com/api/user/add",
                  "method": "POST",
                  "headers": {
                    "Content-Type": "application/json",
                    "token": "${token}"
                  },
                  "queryParams": [
                    {
                      "fieldName": "timestamp",
                      "type": "Long",
                      "required": true
                    }
                  ],
                  "bodyFields": [
                    {
                      "fieldName": "name",
                      "type": "String",
                      "required": true
                    },
                    {
                      "fieldName": "age",
                      "type": "Integer",
                      "required": false,
                      "defaultVal": 18
                    },
                    {
                      "fieldName": "userInfo.phone",
                      "type": "String",
                      "required": false
                    }
                  ]
                }
                """;

        // 2. 业务传入的参数Map（页面/上游服务传入）
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put("token", "abc123456");
        inputParams.put("timestamp", System.currentTimeMillis());
        inputParams.put("name", "张三");
        inputParams.put("userInfo.phone", "13800138000");

        // 3. 加载接口定义
        ApiDefinition apiDef = ApiRequestBuilder.loadApiDef(apiJson);

        // 4. 实例化接口，生成完整请求对象
        ApiRequestBuilder.ApiRequest request = ApiRequestBuilder.buildRequest(apiDef, inputParams);

        // 打印结果
        System.out.println("完整请求URL：" + request.getFullUrl());
        System.out.println("请求头：" + request.getHeaders());
        System.out.println("请求体JSON：" + request.getRequestBody());
    }
}
```

### 输出结果示例
```
完整请求URL：https://xxx.com/api/user/add?timestamp=1789012345678
请求头：{Content-Type=application/json, token=abc123456}
请求体JSON：{"name":"张三","age":18,"userInfo":{"phone":"13800138000"}}
```

## 七、关键要点说明（JsonPath核心用法）
1. **嵌套参数自动处理**
   `fieldName=userInfo.phone` → JsonPath路径 `$.userInfo.phone`，`ctx.set()` 会自动创建中间层级对象，无需手动构造实体类。
2. **无需提前定义POJO**
   全部动态JSON操作，适配动态配置接口场景（接口配置存在数据库，前端可配置字段）。
3. **类型强约束**
   根据元数据中`type`自动转换入参，避免JSON类型错乱（数字变成字符串）。
4. **扩展性**
   - 新增参数校验规则（长度、正则）只需扩展`ParamMeta`增加字段；
   - 支持数组参数：`fieldName=list[0].id`，JsonPath天然支持数组路径；
   - 支持响应结果用JsonPath提取返回值。

## 八、拓展：如果需要读取返回JSON（JsonPath读取响应）
```java
// responseJson 是接口返回字符串
DocumentContext resCtx = JsonPath.parse(responseJson);
// 按配置里的返回路径取值
String username = resCtx.read("$.data.user.name");
```

## 九、落地存储建议
1. 数据库设计：单独一张接口配置表
   - api_id、api_name、api_json（TEXT字段存储上面完整JSON）
2. 缓存：将`ApiDefinition`缓存到Redis，避免每次反序列化JSON
3. 前端可视化配置：页面可视化新增/编辑header、query、body字段，自动组装上述JSON存入库。