package com.salary.calcengine.utils;

import com.salary.calcengine.models.ScriptEngineContext;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ScriptAggregateFunction {
    public static double sumifs(ScriptEngineContext ctx, String targetField, String... conditions) throws Exception {

        // 解析目标表名和字段名
        String[] targetParts = targetField.split("\\.");
        String tableName = targetParts[0];
        String fieldName = targetParts[1];

        val tables = ctx.getTables();
//        val params = ctx.getParam();

        // 获取目标表的数据
        List<Map<String, Object>> tableData = tables.get(tableName);
        if (tableData == null) {
            return 0;
        }

        val finalPredicate = PrepareConditions(ctx, conditions);
//        val tmpResult = tableData
//                .parallelStream()
//                .filter(finalPredicate)
//                .collect(Collectors.toList());

        val sum = tableData
                .parallelStream()
                .filter(finalPredicate)
                .collect(Collectors.summarizingDouble(row -> Double.parseDouble(row.get(fieldName).toString())));

        return sum.getSum();

    }

    private static Predicate<Map<String, Object>> PrepareConditions(ScriptEngineContext ctx, String... conditions) {
        Predicate<Map<String, Object>> p = row -> true;

        // 处理条件
        // TODO: 参数容错
        for (int i = 0; i < conditions.length; i += 3) {
            String conditionField = conditions[i];
            String operator = conditions[i + 1];
            String conditionValueStr = conditions[i + 2];

            // 处理 [always true] 条件
            if ("[always true]".equals(conditionValueStr)) {
                break;
            }

            // 处理变量引用
            if (conditionValueStr.startsWith("${") && conditionValueStr.endsWith("}")) {
                String variableName = conditionValueStr.substring(2, conditionValueStr.length() - 3);
                conditionValueStr = ctx.getParam().get(variableName).toString();
            }

            // TODO: 值类型处理
            final String conditionValueStr1 = conditionValueStr;

            String operatorStr = operator;
            // 处理操作符
            switch (operator) {
                case "==":
                case "=":
                    operatorStr = "==";
                    p = p.and(row -> conditionValueStr1.equals(row.get(conditionField).toString()));

                    break;
                case "!=":
                case "<>":
                case "≠":
                    operatorStr = "!=";
                    p = p.and(row -> !conditionValueStr1.equals(row.get(conditionField).toString()));

                    break;
                case ">":
                    operatorStr = ">";
                    p = p.and(row -> {
                        try {
                            double fieldValueDouble = Double.parseDouble(row.get(conditionField).toString());
                            double conditionValueDouble = Double.parseDouble(conditionValueStr1);
                            return fieldValueDouble > conditionValueDouble;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    });

                    break;
                case ">=":
                case "≥":
                    operatorStr = ">=";
                    p = p.and(row -> {
                        try {
                            double fieldValueDouble = Double.parseDouble(row.get(conditionField).toString());
                            double conditionValueDouble = Double.parseDouble(conditionValueStr1);
                            return fieldValueDouble >= conditionValueDouble;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    });

                    break;
                case "<":
                    operatorStr = "<";
                    p = p.and(row -> {
                        try {
                            double fieldValueDouble = Double.parseDouble(row.get(conditionField).toString());
                            double conditionValueDouble = Double.parseDouble(conditionValueStr1);
                            return fieldValueDouble < conditionValueDouble;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    });

                    break;
                case "<=":
                case "≤":
                    operatorStr = "<=";
                    p = p.and(row -> {
                        try {
                            double fieldValueDouble = Double.parseDouble(row.get(conditionField).toString());
                            double conditionValueDouble = Double.parseDouble(conditionValueStr1);
                            return fieldValueDouble <= conditionValueDouble;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    });

                    break;
                default:
                    throw new RuntimeException(new ScriptFunctionException("Not supported operator"));
            }
        }

        return p;
    }

}
