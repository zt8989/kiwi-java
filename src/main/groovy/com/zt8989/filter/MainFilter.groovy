package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate

/**
 * @author zhouteng
 * @Date 2022/4/7
 */
class MainFilter extends BaseFilter  {
    private final Logger logger = LoggerFactory.getLogger(MainFilter.class.name)
    MainFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 匹配main方法")
        }
        return res
    }

    @Override
    boolean test(Tuple2<StringLiteral, String> node) {
        return intercept(node.getV1()) {
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