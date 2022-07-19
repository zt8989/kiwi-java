package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/14
 */
class EnumFilter extends BaseFilter implements Predicate<StringLiteral>{

    EnumFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${EnumFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${EnumFilter.class.name}] 过滤原因: 定义在ENUM中" )
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral){ !isEnumConstant(stringLiteral) }
    }

    boolean isEnumConstant(StringLiteral stringLiteral) {
        {
            def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, EnumConstantDeclaration.class)
            if (optMethod.isPresent()) {
                return true
            }
            return false
        }
    }
}
