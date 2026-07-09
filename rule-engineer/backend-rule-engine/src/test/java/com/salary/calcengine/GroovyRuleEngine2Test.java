package com.salary.calcengine;


import com.google.common.collect.Maps;
import com.salary.calcengine.groovy.Context2;
import com.salary.calcengine.groovy.GroovyRule2;
import com.salary.calcengine.groovy.GroovyRuleEngine;
import com.salary.calcengine.utils.ScriptFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.Map;


//@SpringBootTest(classes = CalcEngineApplication.class)
//@RunWith(SpringRunner.class)
@Slf4j
public class GroovyRuleEngine2Test {
    @Test
    public void testGroovyEngine2() throws IOException {
        GroovyRuleEngine.initEngine();

        String strategyLogic = """
                if(com.salary.calcengine.utils.ScriptFunction.square(context.input.amount)>=20){
                    context.output.result=true
                } else {
                    context.output.result=false
                }""";
        GroovyRuleEngine.createRule2("rule2", "test2.groovy_templ", strategyLogic);
        GroovyRule2 rule2 = GroovyRuleEngine.getRule2("rule2");

        StopWatch sw = new StopWatch();
        sw.start();
        StopWatch swi = new StopWatch();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> input = Maps.newHashMap();
            input.put("amount", RandomUtils.nextInt(0, 100));
            Map<String, Object> output = Maps.newHashMap();
            Context2 context = new Context2(input, output);

            swi.start();
            rule2.run(context);
            swi.stop();
            System.out.println(JsonUtil.toJsonStr(context) + " : " + swi.getLastTaskTimeNanos() + "ns : " + output.get("result"));
        }
        sw.stop();
        log.info("groovy run : " + sw.getLastTaskTimeMillis());
    }

    @Test
    public void testJava2() {
        StopWatch sw = new StopWatch();
        sw.start();
        StopWatch swi = new StopWatch();
        TestRule2 testRule2 = new TestRule2();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> input = Maps.newHashMap();
            input.put("amount", RandomUtils.nextInt(0, 10));
            Map<String, Object> output = Maps.newHashMap();
            Context2 context = new Context2(input, output);

            swi.start();
            testRule2.run(context);
            swi.stop();
            //System.out.println(JsonUtil.toJsonStr(context) + " : " + swi.getLastTaskTimeNanos() + "ns : " + output.get("result"));
        }
        sw.stop();
        log.info("java run : " + sw.getLastTaskTimeMillis());
    }

    class TestRule2 implements GroovyRule2 {

        @Override
        public void run(Context2 context) {
            if (ScriptFunction.square(Long.valueOf(String.valueOf(context.getInput().get("amount"))).longValue()) >= 20) {
                context.getOutput().put("result", true);
            } else {
                context.getOutput().put("result", false);
            }
        }
    }
}
