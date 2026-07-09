package com.salary.calcengine.service.impl;


import com.salary.calcengine.dto.CalcParamDto;
import com.salary.calcengine.models.ScriptEngineContext;
import com.salary.calcengine.service.ICalcService;
import com.salary.calcengine.utils.ScriptDependencyResolver;
import com.salary.calcengine.utils.ScriptEngineUtil;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class CalcServiceImpl implements ICalcService {
    private String groovyScriptTemplate1 = """
            
            def execScript(ctx) {
                return ($$$)
            }
            
            """;

    @Override
    public Map<String, Object> simpleCalc(CalcParamDto calcParamDto) throws Exception {
        ScriptDependencyResolver resolver = initResolver(calcParamDto.getFormulaMap());

        LinkedHashMap<String, String> orderedScripts = prepareScripts(resolver);

        ScriptEngineContext ctx = new ScriptEngineContext();
        ctx.setTables(calcParamDto.getTables());
        Map<String, Object> params = new LinkedHashMap<>(calcParamDto.getScriptParams());
        ctx.setParam(params);
        for (String key : orderedScripts.keySet()) {
            // log.info(key);
            String script = orderedScripts.get(key);
            // log.info(script);
            Object result = evalScript(script, ctx);
            // Invocable inv = orderedInvocable.get(key);
            // Object result = evalScriptCompiled(inv, parmas);
            // log.info(result.toString());
            params.put(key, result);
        }
        return params;
    }

    @Override
    public Map<String, Object> proCompileCalc(CalcParamDto calcParamDto) throws Exception {


        ScriptDependencyResolver resolver = initResolver(calcParamDto.getFormulaMap());

        LinkedHashMap<String, String> orderedScripts = prepareScripts(resolver);

        StringBuilder sb = new StringBuilder();
        for (String key : orderedScripts.keySet()) {
            String s = orderedScripts.get(key);
            // s = s.replaceAll("agfunc\\.([a-zA-Z0-9_]+)\\(","agfunc.$1(ctx, ");
            String script = groovyScriptTemplate1
                    .replace("execScript", "exec" + key)
                    .replace("$$$", s);
            sb.append(script);
        }
        String scripts = sb.toString();
        Invocable inv = ScriptEngineUtil.getInvocable(ScriptEngineUtil.GROOVY_ENGIN_NAME, scripts);

        ScriptEngineContext ctx = new ScriptEngineContext();
        // 改为从redis获取
        ctx.setTables(calcParamDto.getTables());
        Map<String, Object> params = new LinkedHashMap<>(calcParamDto.getScriptParams());
        ctx.setParam(params);
        for (String key : orderedScripts.keySet()) {
            // log.info(key);
            // String script = orderedScripts.get(key);
            // log.info(script);
            // Object result = evalScript(script, parmas);
            Object result = inv.invokeFunction("exec" + key, ctx);
            // log.info(result.toString());
            params.put(key, result);
        }
        return params;
    }

    private ScriptDependencyResolver initResolver(Map<String, String> formulaMap) {
        // 初始化依赖解析器
        ScriptDependencyResolver resolver = new ScriptDependencyResolver();
        for (String key : formulaMap.keySet()) {
            resolver.addFormula(key, formulaMap.get(key));
        }
        return resolver;
    }

    private LinkedHashMap<String, String> prepareScripts(ScriptDependencyResolver resolver) {
        Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        List<String> scripts = resolver.getCalculationOrder();
        for (String target : scripts) {
            var script = resolver.getFormula(target);
            script = script.replaceAll("agfunc\\.([a-zA-Z0-9_]+)\\(", "agfunc.$1(ctx, ");
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(script);
            script = matcher.replaceAll(match -> "ctx.Param[\"" + match.group(1) + "\"]");
//            for(String p : params.keySet()) {
//                script = script.replace("${" + p + "}", "p[\"" + p + "\"]");
//            }
            map.put(target, script);
        }
        return map;
    }

    private Object evalScript(String script, ScriptEngineContext ctx) throws Exception {
        String ss = groovyScriptTemplate1.replace("$$$", script);
        Object result = ScriptEngineUtil.execGroovyScript(ss, "execScript", ctx);
        return result;
    }
}
