package com.zt8989.transform

import com.zt8989.bean.MuteInt
import com.zt8989.config.Config
import com.zt8989.exception.NoTranslateFoundException
import com.zt8989.translator.Translator
import com.zt8989.util.AstUtils
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite

/**
 * @author zhouteng* @Date 2022/4/6
 */
class MessageFormatTransform extends AbstractTransform{
    MessageFormatTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config) {
        super(astRewrite, compilationUnit, translator, config)
    }

    String replaceMessageFormatWithI18nFormat(String message){
        def count = 0
        return message.replaceAll(/\{}/){
            "{${count++}}"
        }
    }

    void replaceMessageFormatWithI18n(MethodInvocation methodInvocation, StringLiteral stringLiteral){
        def args = []
        StringLiteral code = ASTNode.copySubtree(methodInvocation.getAST(), stringLiteral)
        def value = code.getEscapedValue()
        value = replaceMessageFormatWithI18nFormat(value)
        code.setEscapedValue(value)

        try {
            translateKey(code)
        } catch (NoTranslateFoundException ex){
            return
        }

        List<ASTNode> retainExp = methodInvocation.arguments().subList(1, methodInvocation.arguments().size())
            .collect({ ASTNode.copySubtree(methodInvocation.getAST(), (ASTNode)it) })

        args.add(code)
        def array = ast.newArrayCreation()
        args.add(array)
        array.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("Object"))))
        def arrayInitializer = ast.newArrayInitializer()
        arrayInitializer.expressions().addAll(retainExp)
        array.setInitializer(arrayInitializer)

        convertStringLiteral(args, methodInvocation)
    }

    @Override
    List<StringLiteral> transform(List<StringLiteral> stringLiterals) {
        def cloneList = stringLiterals.collect()
        for(stringLiteral in stringLiterals){
            def methodInvocation = AstUtils.lookupCallerName(stringLiteral) {
                it == "MessageFormat"
            }
            if(methodInvocation.isPresent()){
                replaceMessageFormatWithI18n(methodInvocation.get(), stringLiteral)
                cloneList.remove(stringLiteral)
            }
        }

        return cloneList
    }
}
