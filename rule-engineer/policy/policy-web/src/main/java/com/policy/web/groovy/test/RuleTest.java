package com.policy.web.groovy.test;



import com.google.common.collect.Maps;
import com.googlecode.aviator.AviatorEvaluator;
import com.policy.common.vo.PlainResult;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@RestController
@Tag(name = "User-API", description = "用户")
@Slf4j
public class RuleTest {


    public final String express = "(verifyStatus == 1 && level > 4) || (gender == 2 && age < 2 )";


    @RequestMapping("testGroovy")
    public PlainResult<List> testGroovy(int count) {
        List<Boolean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Boolean result = null;
            ExecutorService executor = Executors.newCachedThreadPool();
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Binding binding = new Binding();
                    binding.setVariable("verifyStatus", 2);
                    binding.setVariable("level", 3);
                    binding.setVariable("gender", 2);
                    binding.setVariable("age", 2);
                    GroovyShell shell = new GroovyShell(binding);
                    Boolean result = (Boolean) shell.evaluate(express);
                    return result;
                }
            });
            executor.submit(futureTask);
            //可以使用所有的Future的接口方法
            while (!futureTask.isDone()) {
                try {
                    // 获取future的结果对象
                    result = futureTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            list.add(result);
        }
        return PlainResult.ok(list);
    }



    @RequestMapping("testAviator")
    public PlainResult<List> testAviator(int count) {
        PlainResult plainResult = new PlainResult();
        List<Boolean> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Boolean result = null;
            ExecutorService executor = Executors.newCachedThreadPool();
            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Map<String, Object> binding = Maps.newConcurrentMap();
                    binding.put("verifyStatus", 2);
                    binding.put("level", 3);
                    binding.put("gender", 2);
                    binding.put("age", 2);
                    AviatorEvaluator.setOptimize(AviatorEvaluator.EVAL);
                    boolean result = (boolean) AviatorEvaluator.execute(express, binding, true);
                    return result;
                }
            });
            executor.submit(futureTask);
            //可以使用所有的Future的接口方法
            while (!futureTask.isDone()) {
                try {
                    // 获取future的结果对象
                    result = futureTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
            list.add(result);
        }
        plainResult.setData(list);
        return plainResult;
    }
}
