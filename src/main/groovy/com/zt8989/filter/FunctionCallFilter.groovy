package com.zt8989.filter


import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import com.zt8989.util.GlobUtils
import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Pattern

/**
 * @author zhouteng* @Date 2022/4/1
 */
class FunctionCallFilter extends BaseFilter  {
    private final Logger logger = LoggerFactory.getLogger(FunctionCallFilter.class.name)
    List<Closure<StringLiteral>> listeners = []
    private Map<String, Pattern> functionCallExcludes


    FunctionCallFilter(Config config) {
        super(config)
        functionCallExcludes = config.yamlConfig.getFunctionCallExcludes().collectEntries({
            [it, Pattern.compile(GlobUtils.createRegexFromGlob(it))]
        })
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            listeners.each { it.call(stringLiteral) }
        }
        return res
    }

    boolean test(Tuple2<StringLiteral, String> node) {
        def res = intercept(node.getV1()) {
            !lookupUntil(it)
        }
        return res
    }

    boolean lookupUntil(StringLiteral stringLiteral){
        def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, MethodInvocation.class)
        if(optMethod.isPresent()){
            def method = optMethod.get()
            def expression = method.getExpression()
            def methodName = method.name
            // this.log.info(xxx)
            if(expression instanceof  FieldAccess){
                def fieldAccess = (FieldAccess) expression
                def fieldName = fieldAccess.getName()
                return shouldMethodFilter("${fieldName}.${methodName}",stringLiteral)
            } else {
                return shouldMethodFilter("${expression}.${methodName}", stringLiteral)
            }
        }
        return false
    }

    def boolean shouldMethodFilter(String name, StringLiteral stringLiteral) {
        return shouldMethodFilter(name).tap {
            if(it.isPresent()){
                logger.info("过滤: {}", stringLiteral.literalValue)
                logger.info("过滤原因: 方法请求过滤规则[" + it.get().key + "]")
            }
        }.map({ true }).orElse(false)
    }

    def shouldMethodFilter(String name) {
        Optional.ofNullable(functionCallExcludes.find {
            name.matches(it.value)
        })
    }
}
