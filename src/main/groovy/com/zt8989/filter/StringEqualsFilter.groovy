package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * "常量".equals等方法
 * @author zhouteng
 * @Date 2022/4/7
 */
class StringEqualsFilter extends BaseFilter implements Predicate<StringLiteral> {
    StringEqualsFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${StringEqualsFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${StringEqualsFilter.class.name}] 过滤原因: 匹配到字符串.equals方法")
        }
        return res
    }

    @Override
    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral) {
            !hasStringEquals(it)
        }
    }

    boolean hasStringEquals(StringLiteral stringLiteral){
        def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, MethodInvocation.class)
        if(optMethod.isPresent()){
            def method = optMethod.get()
            if(method.name.identifier == "equals"){
                return true
            }
        }
        return false
    }
}