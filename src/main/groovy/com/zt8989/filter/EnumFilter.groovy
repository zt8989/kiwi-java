package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/14
 */
class EnumFilter extends BaseFilter {
    private final Logger logger = LoggerFactory.getLogger(EnumFilter.class.name)
    EnumFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 定义在ENUM中" )
        }
        return res
    }

    boolean test(Tuple2<StringLiteral, String> node) {
        return intercept(node.getV1()){ !isEnumConstant(it) }
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
