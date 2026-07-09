package com.salary.calcengine.groovy;

import com.google.common.collect.Maps;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;


/***
 *
 * https://cloud.tencent.com/developer/article/2410130
 * 加载问题处理
 */

@Slf4j
public class GroovyEngineer {


    /**
     * 缓存Script，避免创建太多
     */
    private static final Map<String, Script> SCRIPT_MAP = Maps.newHashMap();

    private static final GroovyClassLoader CLASS_LOADER = new GroovyClassLoader();

    public static Script loadScript(String key, String rule) {
        if (SCRIPT_MAP.containsKey(key)) {
            return SCRIPT_MAP.get(key);
        }
        Script script = loadScript(rule, new Binding());
        SCRIPT_MAP.put(key, script);
        return script;
    }


    public static Script loadScript(String rule, Binding binding) {
        if (StringUtils.isEmpty(rule)) {
            return null;
        }
        try {
            Class ruleClazz = CLASS_LOADER.parseClass(rule);
            if (ruleClazz != null) {
                //log.info("load rule:" + rule + " success!");
                return InvokerHelper.createScript(ruleClazz, binding);
            }
        } catch (Exception e) {
            //log.error(e.getMessage(), e);
        } finally {
            CLASS_LOADER.clearCache();
        }
        return null;
    }


}
