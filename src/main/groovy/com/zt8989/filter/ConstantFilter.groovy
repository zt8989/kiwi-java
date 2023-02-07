package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ConstantFilter extends BaseFilter {
    private final Logger logger = LoggerFactory.getLogger(ConstantFilter.class.name)
    private List<String> constant;

    ConstantFilter(Config config) {
        super(config)
        this.constant = config.yamlConfig.chineseExcludes;
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 用户自定义过滤 {}", constant)
        }
        return res
    }

    boolean test(Tuple2<StringLiteral, String> node) {
        return intercept(node.getV1()){ !constant.contains(it.getLiteralValue()) }
    }
}
