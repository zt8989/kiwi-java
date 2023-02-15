package com.zt8989.filter;

import com.zt8989.config.Config;
import com.zt8989.util.AstUtils;
import groovy.lang.Closure;
import groovy.lang.Tuple2;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhouteng
 */
public class FieldStringFilter extends BaseFilter{
    private final Logger logger = LoggerFactory.getLogger(FieldStringFilter.class.name)
    public FieldStringFilter(Config config) {
        super(config);
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 定义在字段中" )
        }
        return res
    }

    boolean test(Tuple2<StringLiteral, String> node) {
        return intercept(node.getV1()){ !AstUtils.isFieldDeclaration(it).isPresent() }
    }

}
