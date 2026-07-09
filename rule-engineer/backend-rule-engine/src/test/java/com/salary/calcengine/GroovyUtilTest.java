package com.salary.calcengine;


import com.salary.calcengine.groovy.GroovyUtil;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StopWatch;


@Slf4j
public class GroovyUtilTest {
    @Test
    public void test() {
        StopWatch sw = new StopWatch();

        sw.start("compile");
        GroovyObject script = GroovyUtil.loadScript("class Foo { void doIt() { println \"ok\" } }");
        sw.stop();
        log.info("compile : " + sw.getLastTaskTimeMillis());

        sw.start("first invoke");
        script.invokeMethod("doIt", null);
        sw.stop();
        log.info("first invoke : " + sw.getLastTaskTimeMillis());

        sw.start("invoke");
        for(int i=0;i<1000;i++) {
            script.invokeMethod("doIt", null);
        }
        sw.stop();
        log.info("invoke : " + sw.getLastTaskTimeMillis());
    }
}
