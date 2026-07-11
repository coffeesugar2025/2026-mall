package com.policy.web.service.impl;

import com.policy.core.value.ValueType;
import com.policy.web.service.SymbolService;
import com.policy.web.vo.symbol.SymbolResponse;
import com.policy.core.condition.Operator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author 
 * @date 2020/7/14
 * @since 1.0.0
 */
@Service
public class SymbolServiceImpl implements SymbolService {

    /**
     * 规则引擎运算符
     *
     * @param value 例如：CONTROLLER
     * @return >,<,=..
     */
    @Override
    public List<SymbolResponse> getByType(String value) {
        ValueType valueType = ValueType.getByValue(value);
        List<Operator> symbol = valueType.getSymbol();
        return symbol.stream().map(m -> {
            SymbolResponse symbolResponse = new SymbolResponse();
            symbolResponse.setName(m.name());
            symbolResponse.setExplanation(m.getExplanation());
            symbolResponse.setSymbol(m.getSymbol());
            return symbolResponse;
        }).collect(Collectors.toList());
    }
}
