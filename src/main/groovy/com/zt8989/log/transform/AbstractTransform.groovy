package com.zt8989.log.transform

import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

abstract class AbstractTransform {
    ASTRewrite astRewrite
    CompilationUnit compilationUnit
    Config config

    AST getAst(){
        return compilationUnit.getAST();
    }

    AbstractTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Config config){
        this.astRewrite = astRewrite
        this.compilationUnit = compilationUnit
        this.config = config
    }
    abstract public List<MethodInvocation> transform(List<MethodInvocation> methodInvocations);
}
