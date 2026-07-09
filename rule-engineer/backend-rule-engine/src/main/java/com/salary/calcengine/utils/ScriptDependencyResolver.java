package com.salary.calcengine.utils;

import java.util.*;


public class ScriptDependencyResolver {
    /**
     * 存储公式和参数依赖关系
     */
    private final Map<String, String> formulaMap = new HashMap<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();

    /**
     * 添加公式
     */
    public void addFormula(String target, String formula) {
        formulaMap.put(target, formula);
        dependencyGraph.put(target, new HashSet<>());
        parseDependencies(target, formula);
    }

    public String getFormula(String target) {
        if (formulaMap.containsKey(target)) {
            return formulaMap.get(target);
        }
        return "";
    }

    /**
     * 解析公式中的依赖参数
     */
    private void parseDependencies(String target, String formula) {

        Set<String> dependencies = new HashSet<>();
        /* 假设公式中的参数用 ${} 包裹，例如 ${基本工资} */
        int startIndex = formula.indexOf("${");
        while (startIndex != -1) {
            int endIndex = formula.indexOf("}", startIndex);
            if (endIndex == -1) {
                break;
            }
            String dependency = formula.substring(startIndex + 2, endIndex);
            dependencies.add(dependency);
            startIndex = formula.indexOf("${", endIndex);
        }
        dependencyGraph.put(target, dependencies);
    }

    /**
     * 获取计算顺序（拓扑排序）
     */
    public List<String> getCalculationOrder() {
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String node : dependencyGraph.keySet()) {
            if (!visited.contains(node)) {
                topologicalSort(node, visited, visiting, order);
            }
        }

        return order;
    }

    /**
     * 拓扑排序递归实现
     */
    private void topologicalSort(String node, Set<String> visited, Set<String> visiting, List<String> order) {
        if (visiting.contains(node)) {
            throw new IllegalStateException("Circular dependency detected: " + node);
        }
        if (!visited.contains(node)) {
            visiting.add(node);
            for (String dependency : dependencyGraph.get(node)) {
                if (dependencyGraph.containsKey(dependency)) {
                    topologicalSort(dependency, visited, visiting, order);
                }
            }
            visiting.remove(node);
            visited.add(node);
            order.add(node);
        }
    }
}
