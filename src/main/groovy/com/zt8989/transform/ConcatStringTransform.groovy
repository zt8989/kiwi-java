package com.zt8989.transform

import com.zt8989.bean.MuteInt
import com.zt8989.config.Config
import com.zt8989.exception.NoTranslateFoundException
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.InfixExpression
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ConcatStringTransform extends StringTransform{

    ConcatStringTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config) {
        super(astRewrite, compilationUnit, translator, config)
    }

    void convertInfixExpression(InfixExpression expression, List<String> constant, List<StringLiteral> retainStringLiteral, List<ASTNode> retainExp, MuteInt count){
        if(expression.leftOperand instanceof StringLiteral){
            constant.add(expression.leftOperand.literalValue)
            retainStringLiteral.add(expression.leftOperand)
        } else if(expression.leftOperand instanceof InfixExpression){
            convertInfixExpression((InfixExpression)expression.leftOperand, constant, retainStringLiteral, retainExp, count)
        }else {
            constant.add("{$count}".toString())
            retainExp.add(ASTNode.copySubtree(expression.getAST(), expression.leftOperand))
            count++
        }
        if(expression.rightOperand instanceof StringLiteral){
            constant.add(expression.rightOperand.literalValue)
            retainStringLiteral.add(expression.rightOperand)
        } else if(expression.rightOperand instanceof InfixExpression){
            convertInfixExpression((InfixExpression)expression.rightOperand, constant, retainStringLiteral, retainExp, count)
        }else {
            constant.add("{$count}".toString())
            retainExp.add(ASTNode.copySubtree(expression.getAST(), expression.rightOperand))
            count++
        }
        if(expression.hasExtendedOperands()){
            for(ASTNode exp in expression.extendedOperands()){
                if(exp instanceof StringLiteral){
                    retainStringLiteral.add(exp)
                    constant.add(exp.literalValue)
                } else if(exp instanceof InfixExpression){
                    convertInfixExpression((InfixExpression)exp, constant, retainStringLiteral, retainExp, count)
                } else {
                    constant.add("{$count}".toString())
                    retainExp.add(ASTNode.copySubtree(expression.getAST(), exp))
                    count++
                }
            }
        }
    }

    void convertInfixExpression(InfixExpression expression, List<StringLiteral> stringLiterals){
        def constant = []
        def retainStringLiteral = []
        List<ASTNode> retainExp = []
        def count = [
            ref: 0
        ] as MuteInt
        convertInfixExpression(expression, constant, retainStringLiteral, retainExp, count)
        stringLiterals.removeAll(retainStringLiteral)
        def args = []
        def code = ast.newStringLiteral()
        code.setLiteralValue(constant.join())
        try {
            translateKey(code)
        } catch (NoTranslateFoundException ex){
            return
        }

        args.add(code)
        def array = ast.newArrayCreation()
        args.add(array)
        array.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object"))))
        def arrayInitializer = ast.newArrayInitializer()
        arrayInitializer.expressions().addAll(retainExp)
        array.setInitializer(arrayInitializer)

        convertStringLiteral(args, expression)
    }

    List<StringLiteral> transform(List<StringLiteral> stringLiterals) {
        def findInfix = true
        def cloneList = stringLiterals.collect()
        while (findInfix && cloneList.size() > 0) {
            for(stringLiteral in cloneList){
                def parent = stringLiteral.getParent()
                if(parent instanceof InfixExpression){
                    parent = (InfixExpression)parent
                    while (parent.getParent() instanceof InfixExpression){
                        parent = (InfixExpression)parent.getParent()
                    }
                    convertInfixExpression(parent, cloneList)
                    findInfix = true
                    break
                } else {
                    findInfix = false
                }
            }
        }
        return cloneList
    }
}
