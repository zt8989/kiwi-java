package com.zt8989.filter


import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.FieldAccess
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author zhouteng* @Date 2022/4/1
 */
class FunctionCallFilter extends BaseFilter  {
    private final Logger logger = LoggerFactory.getLogger(FunctionCallFilter.class.name)
    List<Closure<StringLiteral>> listeners = []
    private List<Tuple2<String, String>> functionCallExcludes


    FunctionCallFilter(Config config) {
        super(config)
        functionCallExcludes = config.yamlConfig.getFunctionCallExcludes().collect({
            def fieldMethod = it.split("\\.")
            new Tuple2<>(fieldMethod[0], fieldMethod[1])
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

    Optional<Tuple2<String, String>> isSameIdentifierName(String name) {
        Optional.ofNullable(functionCallExcludes.find {
            (it.getV1() == name)
        })
    }

    boolean lookupUntil(StringLiteral stringLiteral){
        def optMethod = AstUtils.lookupUntilASTNode(stringLiteral, MethodInvocation.class)
        if(optMethod.isPresent()){
            def expression = optMethod.get().getExpression()
            def methodName = optMethod.get().name
            // log.info(xxx)
            if(expression instanceof SimpleName){
                def simpleName = (SimpleName)expression
                return shouldMethodFilter(simpleName, methodName, stringLiteral)
                // this.log.info(xxx)
            } else if(expression instanceof  FieldAccess){
                def fieldAccess = (FieldAccess)expression
                def fieldName = fieldAccess.getName()
                return shouldMethodFilter(fieldName, methodName, stringLiteral)
            }
        }
        return false
    }

    def boolean shouldMethodFilter(SimpleName simpleName, SimpleName methodName, StringLiteral stringLiteral) {
        def match = isSameIdentifierName(simpleName.getIdentifier())
        return shouldMethodFilter(match, methodName).tap {
            if(it){
                logger.info("过滤: {}", stringLiteral.literalValue)
                logger.info("过滤原因: 方法请求过滤规则[" + match.get().join(".") + "]")
            }
        }
    }

    def boolean shouldMethodFilter(Optional<Tuple2<String, String>> match, SimpleName methodName) {
        if (match.isPresent()) {
            if (match.get().getV2() == "*") {
                return true
            }
            if (methodName instanceof SimpleName && match.get().getV2() == methodName.getIdentifier()) {
                return true
            }
        }
        return false
    }
}
