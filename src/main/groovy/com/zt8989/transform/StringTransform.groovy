package com.zt8989.transform

import com.zt8989.config.Config
import com.zt8989.exception.NoTranslateFoundException
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

/**
 * @author zhouteng
 * @Date 2022/4/2
 */
class StringTransform extends AbstractTransform{

    StringTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config) {
        super(astRewrite, compilationUnit, translator, config)
    }

    List<StringLiteral> transform(List<StringLiteral> stringLiterals) {
        for(stringLiteral in stringLiterals){
            try {
                convertStringLiteral([stringLiteral].collect {
                    StringLiteral node = ASTNode.copySubtree(it.parent.AST, it)
                    translateKey(node)
                    return node
                }, stringLiteral)
            } catch (NoTranslateFoundException ex){

            }
        }
        return []
    }
}
