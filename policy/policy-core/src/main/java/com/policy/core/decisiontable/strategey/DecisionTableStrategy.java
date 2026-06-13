
package com.policy.core.decisiontable.strategey;


import com.policy.core.Input;
import com.policy.core.PolicyConfiguration;
import com.policy.core.decisiontable.Coll;
import com.policy.core.decisiontable.CollHead;
import com.policy.core.decisiontable.Row;
import com.policy.core.value.Value;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;


public interface DecisionTableStrategy {

    /**
     * 先从高优先级规则执行，返回命中的最高优先级所有结果
     *
     * @param collHeadCompareMap 表头比较器
     * @param decisionTree       决策树
     * @param input              决策表输入参数
     * @param configuration      规则引擎配置信息
     * @return 命中的结果值
     */
    List<Object> compute(@NonNull Map<Integer, CollHead.Comparator> collHeadCompareMap, @NonNull Map<Integer, List<Row>> decisionTree, @NonNull Input input, @NonNull PolicyConfiguration configuration);

    /**
     * 获取row的执行结果
     *
     * @param collHeadCompareMap 表头比较器
     * @param row                一行规则
     * @param input              用户输入参数
     * @param configuration      规则引擎配置信息
     * @return action
     */
    default Value getActionByRow(Map<Integer, CollHead.Comparator> collHeadCompareMap, Row row, Input input, PolicyConfiguration configuration) {
        List<Coll> colls = row.getColls();
        // 校验此行单元格条件是否成立
        for (int i = 0; i < colls.size(); i++) {
            Coll coll = colls.get(i);
            // 单元格为空，无条件跳过
            if (coll == null || coll.getRightValue() == null) {
                continue;
            }
            // 获取到表头比较器，与下面单元格比较
            CollHead.Comparator collHeadCompare = collHeadCompareMap.get(i);
            /*
             * 右值可以有固定值变量，固定值，无元素 input=null
             * 更新：决策表单元格变量可以为函数型变量
             */
            Object rValue = coll.getRightValue().getValue(input, configuration);
            if (!collHeadCompare.compare(rValue)) {
                // 单元格内条件只要有一个不成立，则比较失败
                return null;
            }
        }
        // 所有条件成立，返回结果
        return row.getAction();
    }

}
