package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/7
 */
class MainFilter extends BaseFilter implements Predicate<StringLiteral> {
    MainFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${MainFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${MainFilter.class.name}] 过滤原因: 匹配main方法")
        }
        return res
    }

    @Override
    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral) {
            !isMainMethod(it)
        }
    }

    boolean isMainMethod(StringLiteral stringLiteral){
        {
            def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, MethodDeclaration.class)
            if(optMethod.isPresent()){
                def method = optMethod.get()
                def methodName = method.getName().getIdentifier()
                return methodName == "main"
            }
            return false
        }
    }
}