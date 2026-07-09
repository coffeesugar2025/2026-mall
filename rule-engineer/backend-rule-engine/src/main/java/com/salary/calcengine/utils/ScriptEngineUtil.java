package com.salary.calcengine.utils;

import com.google.common.collect.Maps;
import com.googlecode.aviator.script.AviatorScriptEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import javax.script.*;
import java.util.Map;
import java.util.function.Function;



@Slf4j
public class ScriptEngineUtil {
    private static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private static Map<String, ScriptEngine> scriptEngineMap = Maps.newHashMap();

    public static final String GROOVY_ENGIN_NAME = "groovy";
    public static final String AVIATOR_ENGIN_NAME = "Aviator";

    static {
        scriptEngineMap.put(GROOVY_ENGIN_NAME, scriptEngineManager.getEngineByName(GROOVY_ENGIN_NAME));
        scriptEngineMap.put(AVIATOR_ENGIN_NAME, scriptEngineManager.getEngineByName(AVIATOR_ENGIN_NAME));

        try {
            scriptEngineMap.get(GROOVY_ENGIN_NAME).put("out", System.out);
            scriptEngineMap.get(GROOVY_ENGIN_NAME).put("func", new ScriptFunction());
            scriptEngineMap.get(GROOVY_ENGIN_NAME).put("agfunc", new ScriptAggregateFunction());
            scriptEngineMap.get(GROOVY_ENGIN_NAME).put("square2", (Function<Integer, Integer>) n -> n*n);
            scriptEngineMap.get(GROOVY_ENGIN_NAME).eval("""
                    def add(int i, int j) {
                       return i + j
                    }
                    """);

            ((AviatorScriptEngine)scriptEngineMap.get(AVIATOR_ENGIN_NAME)).getEngine().importFunctions(RandomUtils.class);
            ((AviatorScriptEngine)scriptEngineMap.get(AVIATOR_ENGIN_NAME)).getEngine().addStaticFunctions("Func", ScriptFunction.class);
            scriptEngineMap.get(AVIATOR_ENGIN_NAME).put("funcObj", new ScriptFunction());
            scriptEngineMap.get(AVIATOR_ENGIN_NAME).eval("""
                    fn add(i, j) {
                       return i + j;
                    }
                    """);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    public static Invocable getInvocable(String engineName, String script) throws ScriptException {
        Compilable compilable = (Compilable) scriptEngineMap.get(engineName);
        CompiledScript compileScript = compilable.compile(script);
        Object obj = compileScript.eval();
        Invocable inv = (Invocable) compileScript.getEngine();
        Bindings bindings = compileScript.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
        return inv;
    }

    public static Object execGroovyScript(String script, String methodName,  Object... params) throws ScriptException, NoSuchMethodException {
        Invocable inv = getInvocable(GROOVY_ENGIN_NAME, script);
        return inv.invokeFunction(methodName, params);
    }

    public static Object execGroovyScript(String script,  Object... params) throws ScriptException, NoSuchMethodException {
        String groovyScriptTemplate = """
            def execScript(Object... params) {
                return ($$$)
            }
            
            """;

        Invocable inv = getInvocable(GROOVY_ENGIN_NAME, groovyScriptTemplate.replace("$$$",script));
        return inv.invokeFunction("execScript", params);
    }

    public static Object execAviatorScript(String script, String methodName, Object... params) throws ScriptException, NoSuchMethodException {
        Invocable inv = getInvocable(AVIATOR_ENGIN_NAME, script);
        return inv.invokeFunction(methodName, params);
    }

}
