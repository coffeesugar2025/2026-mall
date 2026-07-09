package com.salary.calcengine.groovy;

import com.google.common.collect.Maps;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.syntax.Types;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Slf4j
public class GroovyUtil {
    private static Map<String, GroovyObject> groovyObjectCache = Maps.newConcurrentMap();

    public static GroovyObject loadScript(String script) {
        if (StringUtils.isEmpty(script)) {
            throw new RuntimeException("script is empty");
        }

        String cacheKey = DigestUtils.md5DigestAsHex(script.getBytes());
        if (groovyObjectCache.containsKey(cacheKey)) {
            log.debug("groovyObjectCache hit");
            return groovyObjectCache.get(cacheKey);
        }

        // groovy安全设置
        final SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setClosuresAllowed(true);            // 禁止使用闭包
        List<Integer> tokensBlacklist = new ArrayList();
        tokensBlacklist.add(Types.KEYWORD_WHILE);   // 添加关键字黑名单 while和goto
        tokensBlacklist.add(Types.KEYWORD_GOTO);
        secure.setTokensBlacklist(tokensBlacklist);
        secure.setIndirectImportCheckEnabled(true); // 设置直接导入检查
        secure.setImportsBlacklist(Arrays.asList("com.alibaba.fastjson.JSONObject"));   // 禁止import包
        secure.setStatementsBlacklist(Arrays.asList(WhileStatement.class)); // statement 黑名单，不能使用while循环块
        final CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(secure);
        GroovyClassLoader classLoader = new GroovyClassLoader(GroovyUtil.class.getClassLoader(), config);

        try {
            Class<?> groovyClass = classLoader.parseClass(script);
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            classLoader.clearCache();

            groovyObjectCache.put(cacheKey, groovyObject);
            log.info("groovy loadScript success: {}", groovyObject);
            return groovyObject;
        } catch (Exception e) {
            throw new RuntimeException("loadScript error", e);
        } finally {
            try {
                classLoader.close();
            } catch (IOException e) {
                log.error("close GroovyClassLoader error", e);
            }
        }
    }

    public static Map<String, Object> invokeMethod2Map(GroovyObject scriptObject, String invokeMethod, Object[] params) {
        return (Map<String, Object>) scriptObject.invokeMethod(invokeMethod, params);
    }

    public static boolean invokeMethod2Boolean(GroovyObject scriptObject, String invokeMethod, Object[] params) {
        return (Boolean) scriptObject.invokeMethod(invokeMethod, params);
    }

    public static String invokeMethod2String(GroovyObject scriptObject, String invokeMethod, Object[] params) {
        log.debug("GroovyObject class: {}", scriptObject.getClass().getSimpleName());
        return (String) scriptObject.invokeMethod(invokeMethod, params);
    }
}
