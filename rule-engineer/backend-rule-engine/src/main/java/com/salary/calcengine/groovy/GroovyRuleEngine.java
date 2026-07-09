package com.salary.calcengine.groovy;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;


@Slf4j
public class GroovyRuleEngine {
    private static Map<String, String> builderCache = Maps.newHashMap();
    private static Map<String, GroovyRule> ruleCache = Maps.newConcurrentMap();
    private static Map<String, GroovyRule2> ruleCache2 = Maps.newConcurrentMap();

    public static void initEngine() throws IOException {
        final String path = "classpath:/groovy/*.groovy_templ";
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Arrays.stream(resolver.getResources(path))
                .forEach(resource -> {
                    try {
                        String fileName = resource.getFilename();
                        InputStream input = resource.getInputStream();
                        InputStreamReader reader = new InputStreamReader(input);
                        BufferedReader br = new BufferedReader(reader);
                        StringBuilder template = new StringBuilder();
                        for (String line; (line = br.readLine()) != null; ) {
                            template.append(line).append("\n");
                        }
                        builderCache.put(fileName, template.toString());
                    } catch (Exception e) {
                        log.error("resolve file failed", e);
                    }
                });
    }

    public static void createRule(String ruleName, String builderName, String ruleStrategy) {
        StopWatch sw = new StopWatch();

        sw.start();
        String scriptBuilder = builderCache.get(builderName);
        String groovyScript = String.format(scriptBuilder, ruleName, ruleStrategy);
        GroovyRule rule = (GroovyRule) GroovyUtil.loadScript(groovyScript);
        sw.stop();
        log.info("load groovy script run : " + sw.getLastTaskTimeMillis());

        sw.start();
        rule.run(null);
        sw.stop();
        log.info("warm groovy script run(ns) : " + sw.getLastTaskTimeNanos());

        ruleCache.put(ruleName, rule);
    }

    public static void createRule2(String ruleName, String builderName, String ruleStrategy) {
        StopWatch sw = new StopWatch();

        sw.start();
        String scriptBuilder = builderCache.get(builderName);
        String groovyScript = String.format(scriptBuilder, ruleName, ruleStrategy);
        GroovyRule2 rule = (GroovyRule2) GroovyUtil.loadScript(groovyScript);
        sw.stop();
        log.info("load groovy script run : " + sw.getLastTaskTimeMillis());

        sw.start();
        rule.run(null);
        sw.stop();
        log.info("warm groovy script run(ns) : " + sw.getLastTaskTimeNanos());

        ruleCache2.put(ruleName, rule);
    }

    public static GroovyRule getRule(String ruleName) {
        return ruleCache.get(ruleName);
    }

    public static GroovyRule2 getRule2(String ruleName) {
        return ruleCache2.get(ruleName);
    }
}
