package com.zt8989.log.filter

import org.eclipse.jdt.core.dom.InfixExpression
import org.eclipse.jdt.core.dom.MethodInvocation

import java.util.function.Predicate

public class InfixExpressionFilter implements Predicate<MethodInvocation> {
    @Override
    public boolean test(MethodInvocation methodInvocation) {
        return isInfixExpression(methodInvocation)
    }

    boolean isInfixExpression(MethodInvocation methodInvocation){
        List arguments = methodInvocation.arguments();
        if(arguments.size() > 0){
            if(arguments[0] instanceof InfixExpression){
                return true;
            }
        }
        return false
    }
}
