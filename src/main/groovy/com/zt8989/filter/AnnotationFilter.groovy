package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.Annotation
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.NormalAnnotation
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * @author zhouteng
 * @Date 2022/4/2
 */
public class AnnotationFilter extends BaseFilter implements Predicate<StringLiteral> {
    private final Logger logger = LoggerFactory.getLogger(AnnotationFilter.class.name)

    List<Tuple2<String, String>> excludeAnnotationList = [
            new Tuple2("AssertFalse", "message"),
            new Tuple2("AssertTrue", "message"),
            new Tuple2("DecimalMax", "message"),
            new Tuple2("DecimalMin", "message"),
            new Tuple2("Digits", "message"),
            new Tuple2("Email", "message"),
            new Tuple2("Future", "message"),
            new Tuple2("FutureOrPresent", "message"),
            new Tuple2("Max", "message"),
            new Tuple2("Min", "message"),
            new Tuple2("Negative", "message"),
            new Tuple2("NegativeOrZero", "message"),
            new Tuple2("NotBlank", "message"),
            new Tuple2("NotEmpty", "message"),
            new Tuple2("NotNull", "message"),
            new Tuple2("Null", "message"),
            new Tuple2("Past", "message"),
            new Tuple2("PastOrPresent", "message"),
            new Tuple2("Pattern", "message"),
            new Tuple2("Positive", "message"),
            new Tuple2("PositiveOrZero", "message"),
            new Tuple2("Size", "message"),
    ]

    AnnotationFilter(Config config) {
        super(config)
    }

    boolean test(StringLiteral stringLiteral) {
        return intercept(stringLiteral) { !isInAnnotation(it) }
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.info("过滤: {}", stringLiteral.literalValue)
            logger.info("过滤原因: 存在用注解中" )
        }
        return res
    }

    boolean shouldExclude(MemberValuePair memberValuePair, Annotation annotation){
        return excludeAnnotationList.any({
            if(annotation instanceof NormalAnnotation){
                def normaAnnotation = annotation as NormalAnnotation
                return memberValuePair.name.identifier == it.v2 && normaAnnotation.typeName?.identifier == it.v1
            }
            return false
        })
    }


    boolean isInAnnotation(StringLiteral stringLiteral){
        def parent = stringLiteral.parent
        while (parent != null && !(parent instanceof TypeDeclaration)) {
            if(parent instanceof Annotation){
                def memberValuePair = AstUtils.lookupUntilASTNode(stringLiteral, MemberValuePair.class)
                if(memberValuePair.isPresent()){
                    return !shouldExclude(memberValuePair.get(), parent)
                }
                return true
            }
            parent = parent.parent
        }
        return false
    }
}
