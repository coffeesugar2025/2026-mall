
### 技术栈/版本介绍：
- 所涉及的相关的技术有：
    - SpringBoot 2.1.2
    - RabbitMQ
    - Redis、Redisson
    - MySQL 5.7.9
    - Mybatis-plus（后期有时间可能去除Mybatis-plus）
    - Maven 3.3
    - Swagger  

### 规则如何调用


配置完成后我们就可以调用接口来执行引擎中的规则了  
```
POST http://ruleserver.cn/Policy/generalRule/execute
Content-Type: application/json

{
      "code": "phoneRuletest",
      "workspaceCode": "default",
      "accessKeyId": "略", 
      "accessKeySecret": "略",
      "param": {
            "phone": "13400000000"
      }
}
```

现在我们让此使用方式更加简单易用！
调用规则方项目pom.xml文件引入以下依赖
```pom
    <dependency>
        <groupId>com.policy</groupId>
        <artifactId>policy-client</artifactId>
        <version>2.0</version>
    </dependency>
```
SpringBoot项目application.yml配置文件配置如下：
```yml
rule.engine:
  baseUrl: http://ruleserver.cn
  workspaceCode: default
  accessKeyId: root
  accessKeySecret: 123456
  # 可选配置
  feignConfig:
    request:
      connectTimeoutMillis: 3000
      readTimeoutMillis: 3500
    retryer:
      period: 2000
      maxPeriod: 2000
      maxAttempts: 3
```
然后编写如下代码进行测试：  
```java
@EnablePolicy
@RunWith(SpringRunner.class)
@SpringBootTest
public class RuleTest {

    @Resource
    private PolicyClient PolicyClient;

    @Test
    public void test() {
        // 构建规则请求参数
        PhoneTestRule phoneTestRule = new PhoneTestRule();
        phoneTestRule.setPhone("134000000000");
        // 调用执行引擎中的规则
        GeneralRule generalRule = this.PolicyClient.generalRule();
        Output output = generalRule.execute(phoneTestRule);
        System.out.println(output);
    }

}

@Data
@Model(code = "phoneRuletest")
public class PhoneTestRule {

    /**
     * ElementField可选，默认code为属性name
     */
    @ElementField(code = "phone")
    private String phone;

}
```

我们默认使用Feign请求，当然你也可以自定义，只需要在项目中配置如下代码：
```java
@Component
@Import({RestTemplate.class})
public class PolicyClientConfig {

    @Resource
    private RestTemplate restTemplate;

    @Bean
    public GeneralRuleInterface generalRuleInterface() {
        return new GeneralRuleInterface() {
    
            @Override
            public ExecuteResult execute(ExecuteParam executeParam) {
                return restTemplate.postForObject("http://ruleserver.cn/Policy/generalRule/execute", executeParam, ExecuteResult.class);
            }
    
            @Override
            public IsExistsResult isExists(IsExistsParam existsParam) {
                // TODO: 2020/12/30  
                return null;
            }
    
            @Override
            public BatchExecuteResult batchExecute(BatchParam batchParam) {
                // TODO: 2020/12/30  
                return null;
            }
        };
    }

}
```
现在你就已经学会了如何使用，更多使用方式敬请期待！


### 下一步进展
 - 优化代码，提高配置体验（进行中）
 - 规则版本（开发中）  
 - 规则监控（待开发）  
 - 评分卡（待开发）  
 - 决策树（待开发）   
 - 元素组（待开发）
 - 提供规则、规则集、决策表延迟加载功能，以及定时清理长时间未使用的规则、规则集、决策表功能（待开发）  
