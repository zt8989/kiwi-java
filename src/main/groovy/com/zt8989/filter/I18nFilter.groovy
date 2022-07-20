package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * @author zhouteng
 * @Date 2022/4/6
 */
public class I18nFilter extends BaseFilter implements Predicate<StringLiteral> {
    private final Logger logger = LoggerFactory.getLogger(I18nFilter.class.name)

    I18nFilter(Config config) {
        super(config)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 使用调用 {}", config.getI18nClass())
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        def res = intercept(stringLiteral) {
            !lookupUntil(it)
        }
        return res
    }

    boolean isI18nClass(String name) {
        return config.getI18nClass() == name
    }

    boolean lookupUntil(StringLiteral stringLiteral){
        def parent = stringLiteral.getParent()
        while (parent != null && (!(parent instanceof TypeDeclaration))) {
            if(parent instanceof MethodInvocation){
                MethodInvocation methodInvocation = parent as MethodInvocation
                def methodName = methodInvocation.getExpression()
                if(methodName instanceof SimpleName){
                    def simpleName = (SimpleName)methodName
                    if(isI18nClass(simpleName.getIdentifier())){
                        return true
                    }
                }
            }
            parent = parent.getParent()
        }
        return false
    }
}
