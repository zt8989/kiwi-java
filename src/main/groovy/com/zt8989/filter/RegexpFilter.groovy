package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/2
 */
class RegexpFilter extends BaseFilter implements Predicate<StringLiteral> {
    final var REGEXP = ~/^[\s【】。, ，：()\\（）；、{}\d“”]+$/

    RegexpFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${RegexpFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${RegexpFilter.class.name}] 过滤原因: 匹配到正则: " + REGEXP)
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral) {
            !stringLiteral.literalValue.matches(REGEXP)
        }
    }
}
