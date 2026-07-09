package com.salary.calcengine;


import com.salary.calcengine.groovy.Context;
import com.salary.calcengine.groovy.GroovyRule;
import com.salary.calcengine.groovy.GroovyRuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.io.IOException;


@SpringBootTest(classes = CalcEngineApplication.class)
@RunWith(SpringRunner.class)
@Slf4j
public class GroovyRuleEngineTest {
    @Test
    public void testGroovyEngine() throws IOException {
        GroovyRuleEngine.initEngine();

        String strategyLogic = """
                if(context.amount>=20){
                    return true
                } else {
                    return false
                }""";
        GroovyRuleEngine.createRule("rule1", "test1.groovy_templ", strategyLogic);
        GroovyRule rule1 = GroovyRuleEngine.getRule("rule1");

        StopWatch sw = new StopWatch();
        sw.start();
        StopWatch swi = new StopWatch();
        for (int i = 0; i < 10000; i++) {
            swi.start();
            Context context = new Context(RandomUtils.nextInt(0, 100));
            swi.stop();
            //System.out.println(JsonUtil.toJsonStr(context) + " : " + swi.getLastTaskTimeNanos() + "ns : " + rule1.run(context));
        }
        sw.stop();
        log.info("groovy run : " + sw.getLastTaskTimeMillis());
    }

    @Test
    public void testJava() {
        StopWatch sw = new StopWatch();
        sw.start();
        StopWatch swi = new StopWatch();
        TestRule testRule = new TestRule();
        for (int i = 0; i < 10000; i++) {
            swi.start();
            Context context = new Context(RandomUtils.nextInt(0, 100));
            swi.stop();
            //System.out.println(JsonUtil.toJsonStr(context) + " : " + swi.getLastTaskTimeNanos() + "ns : " + testRule.run(context));
        }
        sw.stop();
        log.info("java run : " + sw.getLastTaskTimeMillis());
    }

    class TestRule implements GroovyRule {

        @Override
        public boolean run(Context context) {
            if (context.amount >= 20) {
                return true;
            } else {
                return false;
            }
        }
    }
}
