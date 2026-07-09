package com.salary.calcengine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 描述:计算参数
 *
 * @author wang.jun
 * @date 2025/7/14 10:29
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CalcParamDto {
    //@Schema(description = "公式")
    private Map<String, String> formulaMap;

    //@Schema(description = "计算参数")
    private Map<String, Object> scriptParams;

    //@Schema(description = "聚合函数使用表")
    private Map<String, List<Map<String, Object>>> tables;
}
