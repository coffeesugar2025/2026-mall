package com.policy.web.groovy.test;


import com.googlecode.aviator.AviatorEvaluator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class AviatorTest {


    @Test
    public void testExpress() {
        AviatorEvaluator.setOptimize(AviatorEvaluator.EVAL);

        log.info(AviatorEvaluator.execute("1 + 2 + 3") + "");
        log.info(AviatorEvaluator.execute("3 > 1 && 2 != 4 || true") + "");
        log.info((String) AviatorEvaluator.execute("3 > 0 ? 'yes': no"));// 三元表达式对于两个分支的结果类型并不要求一致，可以是任何类型

        // 以下5个皆为true
        AviatorEvaluator.execute("nil == nil");
        AviatorEvaluator.execute("3 > nil");
        AviatorEvaluator.execute("true != nil");
        AviatorEvaluator.execute("' ' > nil");
        AviatorEvaluator.execute("a == nil");

        // 内置函数
        log.info("string.length('hello') = " + AviatorEvaluator.execute("string.length('hello')"));
        log.info("string.contains('hello', 'h') = " + AviatorEvaluator.execute("string.contains('hello', 'h')"));
        log.info("math.pow(-3, 2) = " + AviatorEvaluator.execute("math.pow(-3, 2)"));
        log.info("math.sqrt(9.0) = " + AviatorEvaluator.execute("math.sqrt(9.0)"));

        final List<String> list = new ArrayList<String>();
        list.add("hello");
        list.add(" world");

        final int[] array = new int[3];
        array[0] = 0;
        array[1] = 1;
        array[2] = 3;

        final Map<String, Date> map = new HashMap<>();
        map.put("date", new Date());


        Map<String, Object> env = new HashMap<>();
        env.put("name", "johnny");
        String str = "'hello ' + name";
        log.info((String) AviatorEvaluator.execute(str, env));
        // 对比, 被@Deprecated的方法
        log.info((String) AviatorEvaluator.exec(str, "johnny"));
        env.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS").format(new Date()));
        env.put("foo", new Foo(100, new Date()));
        env.put("email", "killmesoftly2022@gmail.com");
        env.put("list", list);
        env.put("array", array);
        env.put("map", map);
        // 正则
        log.info((String) AviatorEvaluator.execute("email=~/\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}/ ? $0:'unknow'", env));
        // 嵌套引用
        log.info((String) AviatorEvaluator.execute("'[foo i='+ foo.i + ' year='+(foo.date.year+1900)+ ' month='+foo.date.month +']' ", env));
        // 日期对比
        log.info(AviatorEvaluator.execute("date > '2009-12-20 00:00:00:00' ", env) + "");
        // 数组和列表
        log.info(AviatorEvaluator.execute("list[0]+list[1]+'\nsum of array='+(array[0]+array[1]+array[2]) +'\ntoday is '+map.date ", env) + "");
    }

    @Data
    @AllArgsConstructor
    private static class Foo {
        int i;
        Date date;
    }


}
