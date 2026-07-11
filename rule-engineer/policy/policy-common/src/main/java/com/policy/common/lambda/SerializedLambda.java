package com.policy.common.lambda;
import java.io.Serializable;
public class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 8025925345765570181L;
    private Class<?> capturingClass;
    private String functionalInterfaceClass;
    private String functionalInterfaceMethodName;
    private String functionalInterfaceMethodSignature;
    private String implClass;
    private String implMethodName;
    private String implMethodSignature;
    private int implMethodKind;
    private String instantiatedMethodType;

    public void setCapturingClass(Class<?> capturingClass) {
        this.capturingClass = capturingClass;
    }

    public void setFunctionalInterfaceClass(String functionalInterfaceClass) {
        this.functionalInterfaceClass = functionalInterfaceClass;
    }

    public void setFunctionalInterfaceMethodName(String functionalInterfaceMethodName) {
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
    }

    public void setFunctionalInterfaceMethodSignature(String functionalInterfaceMethodSignature) {
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public void setImplMethodName(String implMethodName) {
        this.implMethodName = implMethodName;
    }

    public void setImplMethodSignature(String implMethodSignature) {
        this.implMethodSignature = implMethodSignature;
    }

    public void setImplMethodKind(int implMethodKind) {
        this.implMethodKind = implMethodKind;
    }

    public void setInstantiatedMethodType(String instantiatedMethodType) {
        this.instantiatedMethodType = instantiatedMethodType;
    }




    public Class<?> getCapturingClass() {
        return this.capturingClass;
    }

    public String getFunctionalInterfaceClass() {
        return this.functionalInterfaceClass;
    }

    public String getFunctionalInterfaceMethodName() {
        return this.functionalInterfaceMethodName;
    }

    public String getFunctionalInterfaceMethodSignature() {
        return this.functionalInterfaceMethodSignature;
    }

    public String getImplClass() {
        return this.implClass;
    }

    public String getImplMethodName() {
        return this.implMethodName;
    }

    public String getImplMethodSignature() {
        return this.implMethodSignature;
    }

    public int getImplMethodKind() {
        return this.implMethodKind;
    }

    public String getInstantiatedMethodType() {
        return this.instantiatedMethodType;
    }
}
