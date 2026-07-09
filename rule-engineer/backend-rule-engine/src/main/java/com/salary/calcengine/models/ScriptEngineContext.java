package com.salary.calcengine.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
public class ScriptEngineContext {
    private Map<String,Object> param = null;

    private Map<String, List<Map<String,Object>>> tables = null;
}
