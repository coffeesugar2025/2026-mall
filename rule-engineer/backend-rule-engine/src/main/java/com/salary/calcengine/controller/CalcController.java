package com.salary.calcengine.controller;


import com.salary.calcengine.dto.CalcParamDto;
import com.salary.calcengine.service.ICalcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/calc")
@Validated
public class CalcController {
    @Autowired
    private ICalcService iCalcService;

    @PostMapping(value = "/simple-calc")
    //@Operation(summary = "数据量少，计算量少的简单计算方法，直接传参")
    public Map<String, Object> simpleCalc(@Validated @RequestBody CalcParamDto calcParamDto) throws Exception {
        return iCalcService.simpleCalc(calcParamDto);
    }

    @PostMapping(value = "/pro-compile-calc")
    //@Operation(summary = "数据量大，计算量大的预编译计算方法，参数存储redis")
    public Map<String, Object> proCompileCalc(@Validated @RequestBody CalcParamDto calcParamDto) throws Exception {
        return iCalcService.proCompileCalc(calcParamDto);
    }

}
