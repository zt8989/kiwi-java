package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * @author zhouteng* @Date 2022/4/1
 */
class LogInfoFilter extends BaseFilter implements Predicate<StringLiteral> {

    List<Closure<StringLiteral>> listeners = []

    LogInfoFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${LogInfoFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${LogInfoFilter.class.name}] 过滤原因: 使用log调用")
            listeners.each { it.call(stringLiteral) }
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        def res = intercept(stringLiteral) {
            !lookupUntil(it)
        }
        return res
    }

    boolean isLogInfo(String name) {
        return  ["log", "logger", "loger"].any {
            it.equalsIgnoreCase(name)
        }
    }

    boolean lookupUntil(StringLiteral stringLiteral){
        def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, MethodInvocation.class)
        if(optMethod.isPresent()){
            def methodName = optMethod.get().getExpression()
            // log.info(xxx)
            if(methodName instanceof SimpleName){
                def simpleName = (SimpleName)methodName
                if(isLogInfo(simpleName.getIdentifier())){
                    return true
                }
                // this.log.info(xxx)
            } else if(methodName instanceof  FieldAccess){
                def fieldAccess = (FieldAccess)methodName
                def fieldName = fieldAccess.getName()
                if(isLogInfo(fieldName.getIdentifier())){
                    return true
                }
            }
        }
        return false
    }
}
