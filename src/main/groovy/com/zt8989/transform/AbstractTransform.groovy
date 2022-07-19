package com.zt8989.transform

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
public abstract class AbstractTransform {
    ASTRewrite astRewrite
    CompilationUnit compilationUnit
    Translator translator
    Config config

    AST getAst(){
        return compilationUnit.getAST();
    }

    AbstractTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config){
        this.astRewrite = astRewrite
        this.compilationUnit = compilationUnit
        this.translator = translator
        this.config = config
    }

    MethodInvocation getI18nCall(List<ASTNode> args){
        def methodInvocation = ast.newMethodInvocation()
        methodInvocation.setExpression(ast.newSimpleName(config.getI18nClass()))
        methodInvocation.name = ast.newSimpleName("getMessage")
        methodInvocation.arguments().addAll(args)
        methodInvocation
    }

    def translateKey(StringLiteral stringLiteral){
        def string = translator.translate(stringLiteral.literalValue)
        if(string.isPresent()){
            stringLiteral.setLiteralValue(string.get())
        }
    }

    void convertStringLiteral(List<ASTNode> args, ASTNode target){
        def methodInvocation = getI18nCall(args)
        astRewrite.replace(target, methodInvocation, null)
    }

    abstract public List<StringLiteral> transform(List<StringLiteral> stringLiterals);
}
