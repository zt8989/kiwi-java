package com.zt8989.log.filter

import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.SimpleName

import java.util.function.Predicate

class FunctionCallFilter implements Predicate<MethodInvocation> {
    private List<Tuple2<String, String>> functionCallExcludes

    FunctionCallFilter(){
        this.functionCallExcludes = ["log.*"].collect({
            def fieldMethod = it.split("\\.")
            new Tuple2<>(fieldMethod[0], fieldMethod[1])
        })
    }

    @Override
    boolean test(MethodInvocation methodInvocation) {
        return checkMethodInvocation(methodInvocation)
    }

    boolean checkMethodInvocation(MethodInvocation methodInvocation){
        def expression = methodInvocation.getExpression()
        def methodName = methodInvocation.name
        // log.info(xxx)
        if(expression instanceof SimpleName){
            def simpleName = (SimpleName)expression
            return shouldMethodFilter(simpleName, methodName)
            // this.log.info(xxx)
        } else if(expression instanceof  FieldAccess){
            def fieldAccess = (FieldAccess)expression
            def fieldName = fieldAccess.getName()
            return shouldMethodFilter(fieldName, methodName)
        }
        return false
    }

    def boolean shouldMethodFilter(SimpleName simpleName, SimpleName methodName) {
        def match = isSameIdentifierName(simpleName.getIdentifier())
        return shouldMethodFilter(match, methodName)
    }

    def boolean shouldMethodFilter(Optional<Tuple2<String, String>> match, SimpleName methodName) {
        if (match.isPresent()) {
            if (match.get().getV2() == "*") {
                return true
            }
            if (methodName instanceof SimpleName && match.get().getV2() == methodName.getIdentifier()) {
                return true
            }
        }
        return false
    }

    Optional<Tuple2<String, String>> isSameIdentifierName(String name) {
        Optional.ofNullable(functionCallExcludes.find {
            (it.getV1() == name)
        })
    }
}
