package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ConstantFilter extends BaseFilter implements Predicate<StringLiteral>{
    private List<String> constant;

    ConstantFilter(Config config) {
        super(config)
        this.constant = config.yamlConfig.chineseExcludes;
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${ConstantFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${ConstantFilter.class.name}] 过滤原因: 用户自定义过滤" + constant)
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral){ !constant.contains(it.getLiteralValue()) }
    }
}
