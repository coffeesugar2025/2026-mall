package com.salary.calcengine;

import com.salary.calcengine.models.ScriptEngineContext;
import com.salary.calcengine.utils.ScriptDependencyResolver;
import com.salary.calcengine.utils.ScriptEngineUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import javax.script.Invocable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class ScriptEngineUtilTest {

    private String groovyScript = """
            def 中文(int i) {
                out.println(square2.apply(17))
                out.println(func.sum(1,2,3,4,5,Math.round(Math.random()*10)))
                return (func.square(17) * add(i, i+1)) / 100
            }
            """;

    private String groovyScript2 = """
            def demo2(String str) {
                out.println(func.concatenate("input: ", '"', str, '"'))
                out.println(func.IF(1>0, str, func.dateValue(str)))
                out.println(func.IF(1<0, str, func.dateValue(str)))
                out.println(func.workdays(func.dateValue(str),func.eoMonth(func.dateValue(str),0)))
                out.println(func.dateFormat(func.eoMonth(func.dateValue(str),0),"yyyy-MM-dd"))
                return func.dateValue(str)
            }
            """;
    private String aviatorScript = """
            fn demo1(i) {
                if(RandomUtils.nextLong(0,100) > 50) {
                    return (Func.cubic(17) * add(i, i+1)) / double(100);
                } else {
                    return Func.square(funcObj, 3);
                }
            }
            """;

    private String groovyPerformanceScript = """
            def demo1(int i) {
                return (func.square(17) * add(i, i+1)) / 100
            }
            """;
    private String aviatorPerformanceScript = """
            fn demo1(i) {
                return (Func.square(17) * add(i, i+1)) / double(100);
            }
            """;

    private String groovyScriptTemplate = """
            def execScript(Map p) {
                return ($$$)
            }
                        
            """;




    private String groovyScriptTemplate1 = """
                        
            def execScript(ctx) {
                return ($$$)
            }
                        
            """;

    // private Map<String, Object> scriptParams = new HashMap<>();


    @Test
    public void testGroovy() throws Exception {
        Object result = ScriptEngineUtil.execGroovyScript(groovyScript, "中文", 1);
        log.info(Objects.toString(result));
    }


    @Test
    public void testGroovy2() throws Exception {
        Object result = ScriptEngineUtil.execGroovyScript(groovyScript2, "demo2", "2025-01-05");
        log.info(Objects.toString(result));
    }

    @Test
    public void testGroovyScriptSet() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();

        ScriptDependencyResolver resolver = initResolver();

        LinkedHashMap<String, String> orderedScripts = prepareScripts(resolver);

        ScriptEngineContext ctx = new ScriptEngineContext();
        ctx.setTables(prepareTableData());
        Map<String, Object> parmas = null;
        for (int i = 0; i < 10000; i++) {
            parmas = InitParam();
            ctx.setParam(parmas);
            for (String key : orderedScripts.keySet()) {
                // log.info(key);
                String script = orderedScripts.get(key);
                // log.info(script);
                Object result = evalScript(script, ctx);
                // Invocable inv = orderedInvocable.get(key);
                // Object result = evalScriptCompiled(inv, parmas);
                // log.info(result.toString());
                parmas.put(key, result);
            }
        }
        sw.stop();
        log.info("run : " + sw.getLastTaskTimeMillis() + "ms");
        log.info("===============");
        for (String p : parmas.keySet()) {
            log.info(p + " = " + parmas.get(p).toString());
        }
        log.info("===============");
    }

    @Test
    public void testProCompileScriptSet() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();

        ScriptDependencyResolver resolver = initResolver();

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
        ctx.setTables(prepareTableData());
        Map<String, Object> parmas = null;
        for (int i = 0; i < 10000; i++) {
            parmas = InitParam();
            ctx.setParam(parmas);
            for (String key : orderedScripts.keySet()) {
                // log.info(key);
                // String script = orderedScripts.get(key);
                // log.info(script);
                // Object result = evalScript(script, parmas);
                Object result = inv.invokeFunction("exec" + key, ctx);
                // log.info(result.toString());
                parmas.put(key, result);
            }
        }
        sw.stop();
        if (parmas != null) {
            log.info("run : " + sw.getLastTaskTimeMillis() + "ms");
            log.info("===============");
            for (String p : parmas.keySet()) {
                log.info(p + " = " + parmas.get(p).toString());
            }
            log.info("===============");
        }
    }

    private Map<String, List<Map<String, Object>>> prepareTableData() {
        Map<String, List<Map<String, Object>>> tables = new HashMap<>();
//        // 模拟表数据
//        List<Map<String, Object>> tableData = List.of(
//                Map.of("部门","A","字段1", 10, "字段2", 12.3, "字段3", 150),
//                Map.of("部门","A","字段1", 20, "字段2", 45.6, "字段3", 200),
//                Map.of("部门","B","字段1", 30, "字段2", 15.3, "字段3", 50)
//        );

        List<Map<String, Object>> tableData = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10000; i++) {
            tableData.add(Map.of("部门", random.nextInt(2) > 0 ? "A" : "B", "字段1", random.nextInt(30), "字段2", random.nextDouble(100), "字段3", 150));
        }

        tables.put("表1", tableData);

        return tables;
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

    private Map<String, Object> InitParam() {
        Map<String, Object> scriptParams = new LinkedHashMap<>();

        scriptParams.put("表2.所属月", "2025-01");
        scriptParams.put("养老金比例", 0.08);
        scriptParams.put("职业年金比例", 0.04);
        scriptParams.put("失业比例", 0.005);
        scriptParams.put("医保比例", 0.02);
        scriptParams.put("公积金比例", 0.12);
        scriptParams.put("简身险金额", 800);

        scriptParams.put("工号", "10001");
        scriptParams.put("姓名", "张三");
        scriptParams.put("基本工资", 7000 + 5000 * Math.random());
        scriptParams.put("预发绩效工资", 5000 + 2000 * Math.random());
        scriptParams.put("社保基数", 7000);
        scriptParams.put("事假天数", 0);
        scriptParams.put("病假天数", 1);
        scriptParams.put("旷工天数", 0);
        scriptParams.put("入职日期", "2024-12-01");
        scriptParams.put("离职日期", "");
        scriptParams.put("试用期开始日期", "2024-12-01");
        scriptParams.put("试用期结束日期", "2025-01-05");
        scriptParams.put("个税", 0);

        return scriptParams;
    }

    private ScriptDependencyResolver initResolver() {
        // 初始化依赖解析器
        ScriptDependencyResolver resolver = new ScriptDependencyResolver();
        resolver.addFormula("测试聚合", "agfunc.sumifs('表1.字段1','部门','=','A')");
        resolver.addFormula("测试聚合2", "agfunc.sumifs('表1.字段1','部门','!=','A')");
        resolver.addFormula("测试聚合3", "agfunc.sumifs('表1.字段1','字段1','>','10')");
        resolver.addFormula("测试聚合4", "agfunc.sumifs('表1.字段1','字段2','<','20.5')");
        resolver.addFormula("测试聚合5", "agfunc.sumifs('表1.字段1')+1");
        resolver.addFormula("测试聚合6", "agfunc.sumifs('表1.字段1','部门','=','[always true]')");
        resolver.addFormula("发薪开始日期", "func.concatenate(${表2.所属月},'-01')");
        resolver.addFormula("发薪结束日期", "func.dateFormat(func.eoMonth(func.dateValue(func.concatenate(${表2.所属月},'-01')),0),'yyyy-MM-dd')");
        resolver.addFormula("当期试用期开始日期", "func.IF((${试用期开始日期}!='' && func.dateValue(${试用期开始日期}) <= func.dateValue(${发薪结束日期})), func.IF(func.dateValue(${试用期开始日期}) < func.dateValue(${发薪开始日期}), func.dateFormat(func.dateValue(${发薪开始日期}), 'yyyy-MM-dd'), func.dateFormat(func.dateValue(${试用期开始日期}), 'yyyy-MM-dd')), '')");
        resolver.addFormula("当期试用期结束日期", "func.IF((${试用期开始日期}!='' && func.dateValue(${试用期开始日期}) <= func.dateValue(${发薪结束日期})), func.IF(${试用期结束日期}=='', func.dateFormat(func.dateValue(${发薪结束日期}), 'yyyy-MM-dd'), func.IF(func.dateValue(${试用期结束日期}) <= func.dateValue(${发薪结束日期}), func.dateFormat(func.dateValue(${试用期结束日期}), 'yyyy-MM-dd'), func.dateFormat(func.dateValue(${发薪结束日期}), 'yyyy-MM-dd'))), '')");
        resolver.addFormula("试用期工作天数", "func.IF((${当期试用期开始日期}!='' && ${当期试用期结束日期}!=''), func.workdays(func.dateValue(${当期试用期开始日期}),func.dateValue(${当期试用期结束日期})), 0)");
        resolver.addFormula("当期正常在职开始日期", "func.IF((${试用期开始日期}!='' && ${试用期结束日期}!='' && func.dateValue(${试用期结束日期}) > func.dateValue(${发薪结束日期})), '', func.IF((${试用期开始日期}!='' && ${试用期结束日期}!='' && func.dateValue(${试用期结束日期}) <= func.dateValue(${发薪结束日期})), func.IF(func.dateValue(${试用期结束日期}) < func.dateValue(${发薪开始日期}), func.dateFormat(func.dateValue(${发薪开始日期}), 'yyyy-MM-dd'), func.dateFormat(func.dateAdd(func.dateValue(${试用期结束日期}), 1), 'yyyy-MM-dd')), func.IF((${入职日期}!='' && func.dateValue(${入职日期}) <= func.dateValue(${发薪结束日期})), func.IF(func.dateValue(${入职日期}) < func.dateValue(${发薪开始日期}), func.dateFormat(func.dateValue(${发薪开始日期}), 'yyyy-MM-dd'), func.dateFormat(func.dateValue(${入职日期}), 'yyyy-MM-dd')), '')))");
        resolver.addFormula("当期正常在职结束日期", "func.IF(${离职日期}=='', func.IF((${入职日期}!='' && func.dateValue(${入职日期}) <= func.dateValue(${发薪结束日期})), func.IF((${试用期开始日期}!='' && ${试用期结束日期}!='' && func.dateValue(${试用期结束日期}) > func.dateValue(${发薪结束日期})), '', func.dateFormat(func.dateValue(${发薪结束日期}), 'yyyy-MM-dd')), ''), func.IF(func.dateValue(${离职日期}) <= func.dateValue(${发薪结束日期}), func.dateFormat(func.dateValue(${离职日期}), 'yyyy-MM-dd'), ''))");
        resolver.addFormula("非试用期工作天数", "func.IF((${当期正常在职开始日期}!='' && ${当期正常在职结束日期}!=''), func.workdays(func.dateValue(${当期正常在职开始日期}),func.dateValue(${当期正常在职结束日期})), 0)");
        resolver.addFormula("试用期应发薪资", "(${基本工资}+${预发绩效工资})*0.8/21.75*${试用期工作天数}");
        resolver.addFormula("非试用期应发薪资", "(${基本工资}+${预发绩效工资})/21.75*${非试用期工作天数}");
        resolver.addFormula("考勤扣款", "${基本工资}/21.75*0.3*${病假天数}+${基本工资}/21.75*${事假天数}+${基本工资}/21.75*${旷工天数}*3");
        resolver.addFormula("应发合计", "${试用期应发薪资}+${非试用期应发薪资}-${考勤扣款}");
        resolver.addFormula("养老金", "${基本工资}*${养老金比例}");
        resolver.addFormula("职业年金", "${基本工资}*${职业年金比例}");
        resolver.addFormula("失业", "${基本工资}*${失业比例}");
        resolver.addFormula("医保", "${基本工资}*${医保比例}");
        resolver.addFormula("住房公积金", "${基本工资}*${公积金比例}");
        resolver.addFormula("简身险", "${简身险金额}");
        resolver.addFormula("社保福利扣款小计", "${养老金}+${职业年金}+${失业}+${医保}+${住房公积金}+${简身险}");
        resolver.addFormula("工会费", "(${基本工资}-${社保福利扣款小计}-${个税})*0.005");
        resolver.addFormula("应扣合计", "${社保福利扣款小计}+${个税}+${工会费}");
        resolver.addFormula("实发金额", "${应发合计}-${应扣合计}");

        return resolver;
    }


    @Test
    public void testAviator() throws Exception {
        Object result = ScriptEngineUtil.execAviatorScript(aviatorScript, "demo1", 1);
        log.info(Objects.toString(result));
    }

    @Test
    public void performanceGroovy() throws Exception {
        StopWatch sw = new StopWatch();

        sw.start("compile");
        Invocable inv = ScriptEngineUtil.getInvocable(ScriptEngineUtil.GROOVY_ENGIN_NAME, groovyPerformanceScript);
        sw.stop();
        log.info("compile : " + sw.getLastTaskTimeMillis() + "ms");

        sw.start("run");
        StopWatch swi = new StopWatch();
        for (int i = 0; i < 10000; i++) {
            swi.start();
            Object result = inv.invokeFunction("demo1", i);
            swi.stop();
            System.out.println(result + " : " + swi.getLastTaskTimeNanos() + "ns");
        }
        sw.stop();
        log.info("run : " + sw.getLastTaskTimeMillis() + "ms");
    }

    @Test
    public void performanceAviator() throws Exception {
        StopWatch sw = new StopWatch();

        sw.start("compile");
        Invocable inv = ScriptEngineUtil.getInvocable(ScriptEngineUtil.AVIATOR_ENGIN_NAME, aviatorPerformanceScript);
        sw.stop();
        log.info("compile : " + sw.getLastTaskTimeMillis() + "ms");

        sw.start("run");
        StopWatch swi = new StopWatch();
        for (int i = 0; i < 10000; i++) {
            swi.start();
            Object result = inv.invokeFunction("demo1", i);
            swi.stop();
            System.out.println(result + " : " + swi.getLastTaskTimeNanos() + "ns");
        }
        sw.stop();
        log.info("run : " + sw.getLastTaskTimeMillis() + "ms");
    }
}
