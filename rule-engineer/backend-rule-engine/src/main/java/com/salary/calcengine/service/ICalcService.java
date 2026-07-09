package com.salary.calcengine.service;

import com.salary.calcengine.dto.CalcParamDto;

import java.util.Map;


public interface ICalcService {
    Map<String, Object> simpleCalc(CalcParamDto calcParamDto) throws Exception;

    Map<String, Object> proCompileCalc(CalcParamDto calcParamDto) throws Exception;
}
