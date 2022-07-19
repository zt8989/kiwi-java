package com.zt8989.transform

import com.zt8989.config.Config
import com.zt8989.translator.Translator
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author zhouteng
 * @Date 2022/4/8
 */
public class AnnotationTransform extends AbstractTransform {
    AnnotationTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config) {
        super(astRewrite, compilationUnit, translator, config)
    }

    Optional<MemberValuePair> isAnnotationWithMessage(StringLiteral stringLiteral){
        def memberValuePair = AstUtils.lookupUntilASTNode(stringLiteral, MemberValuePair.class)
        if(memberValuePair.isPresent() && memberValuePair.get().name.identifier == "message"){
            return memberValuePair
        }
        return Optional.empty()
    }

    @Override
    List<StringLiteral> transform(List<StringLiteral> stringLiterals) {
        List<StringLiteral> cloneList = stringLiterals.clone()

        for(stringLiteral in stringLiterals){
            def memberValuePair = isAnnotationWithMessage(stringLiteral)
            if(memberValuePair.isPresent()){
                transformMessage(stringLiteral)
                cloneList.remove(stringLiteral)
            }
        }

        return cloneList
    }

    void transformMessage(StringLiteral stringLiteral){
        def string = translator.translate(stringLiteral.literalValue)
        if(string.isPresent()){
            astRewrite.set(stringLiteral, StringLiteral.ESCAPED_VALUE_PROPERTY, "\"{${string.get()}}\"".toString(), null)
        }
    }
}
