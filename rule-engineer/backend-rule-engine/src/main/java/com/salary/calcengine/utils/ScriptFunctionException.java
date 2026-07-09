package com.salary.calcengine.utils;


public class ScriptFunctionException extends Exception {
    public ScriptFunctionException() {
        super();
    }


    public ScriptFunctionException(String message) {
        super(message);
    }


    public ScriptFunctionException(String message, Throwable cause) {
        super(message, cause);
    }


    public ScriptFunctionException(Throwable cause) {
        super(cause);
    }


    protected ScriptFunctionException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
