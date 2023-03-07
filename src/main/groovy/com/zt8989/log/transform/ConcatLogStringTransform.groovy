package com.zt8989.log.transform

import com.zt8989.bean.MuteInt
import com.zt8989.config.Config
import com.zt8989.translator.Translator
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.InfixExpression
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ConcatLogStringTransform extends AbstractTransform{
    ConcatLogStringTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Config config){
        super(astRewrite, compilationUnit, config)
    }

    @Override
    List<MethodInvocation> transform(List<MethodInvocation> methodInvocations) {
        def cloneList = methodInvocations.collect()
        for(methodInvocation in cloneList){
            convertInfixExpression(methodInvocation)
        }
        return cloneList
    }

    void convertInfixExpression(InfixExpression expression, List<String> constant, List<StringLiteral> retainStringLiteral, List<ASTNode> retainExp){
        if(expression.leftOperand instanceof StringLiteral){
            constant.add(expression.leftOperand.literalValue)
            retainStringLiteral.add(expression.leftOperand)
        } else if(expression.leftOperand instanceof InfixExpression){
            convertInfixExpression((InfixExpression) expression.leftOperand, constant, retainStringLiteral, retainExp)
        }else {
            constant.add("{}")
            retainExp.add(ASTNode.copySubtree(expression.getAST(), expression.leftOperand))
        }
        if(expression.rightOperand instanceof StringLiteral){
            constant.add(expression.rightOperand.literalValue)
            retainStringLiteral.add(expression.rightOperand)
        } else if(expression.rightOperand instanceof InfixExpression){
            convertInfixExpression((InfixExpression) expression.rightOperand, constant, retainStringLiteral, retainExp)
        }else {
            constant.add("{}")
            retainExp.add(ASTNode.copySubtree(expression.getAST(), expression.rightOperand))
        }
        if(expression.hasExtendedOperands()){
            for(ASTNode exp in expression.extendedOperands()){
                if(exp instanceof StringLiteral){
                    retainStringLiteral.add(exp)
                    constant.add(exp.literalValue)
                } else if(exp instanceof InfixExpression){
                    convertInfixExpression((InfixExpression) exp, constant, retainStringLiteral, retainExp)
                } else {
                    constant.add("{}".toString())
                    retainExp.add(ASTNode.copySubtree(expression.getAST(), exp))
                }
            }
        }
    }

    void convertInfixExpression(MethodInvocation expression){
        def constant = []
        def retainStringLiteral = []
        List<ASTNode> retainExp = []
        def arguments = expression.arguments()
        def args = []

        convertInfixExpression(arguments[0] as InfixExpression, constant, retainStringLiteral, retainExp)

        def code = ast.newStringLiteral()
        code.setLiteralValue(constant.join())

        args.add(code)
        args.addAll(retainExp)
        args.addAll(arguments[1..<arguments.size()].collect({ it -> ASTNode.copySubtree(expression.AST, it as ASTNode)}))

        def invocation = ASTNode.copySubtree(expression.AST, expression) as MethodInvocation
//        ASTNode.copySubtree(invocation.AST, expression.parent)
        invocation.arguments().clear()
        invocation.arguments().addAll(args)
        astRewrite.replace(expression, invocation, null)
    }

}
