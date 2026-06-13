package com.policy.web.groovy.test;


import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class GroovyTest {


    @Test
    public void testExpressFalse() {
        Binding binding = new Binding();
        binding.setVariable("verifyStatus", 2);
        binding.setVariable("level", 3);
        binding.setVariable("gender", 2);
        binding.setVariable("age", 2);
        GroovyShell shell = new GroovyShell(binding);
        boolean result = (boolean) shell.evaluate("(verifyStatus == 1 && level > 4) || (gender == 2 && age < 2 )");
        String message = "执行失败";
        Assert.assertTrue(message, result);
    }


    @Test
    public void testExpressTrue() {
        Binding binding = new Binding();
        binding.setVariable("verifyStatus", -2);
        binding.setVariable("level", 33);
        binding.setVariable("gender", 1);
        binding.setVariable("age", -2);
        GroovyShell shellResult = new GroovyShell(binding);
        boolean resultShell = (boolean) shellResult.evaluate("(verifyStatus < 0 && level > 4) || (gender == 1 && age < 2 )");
        String message = "执行成功";
        Assert.assertTrue(message, resultShell);
    }


}
