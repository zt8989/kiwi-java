package com.zt8989.filter

import com.zt8989.bean.ConfigDto
import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.Comment
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.TypeDeclaration

import java.util.function.Predicate

/**
 * @author zhouteng* @Date 2022/4/2
 */
class MethodFilter extends BaseFilter implements Predicate<StringLiteral> {
    final var methodComment = "kiwi-disable-method"
    CompilationUnit compilationUnit
    List<Comment> comments

    MethodFilter(Config config, CompilationUnit compilationUnit) {
        super(config)
        setCompilationUnit(compilationUnit)
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            println("[${MethodFilter.class.name}] 过滤: " + stringLiteral.literalValue)
            println("[${MethodFilter.class.name}] 过滤原因: 包含注释: " + methodComment)
        }
        return res
    }

    boolean test(StringLiteral stringLiteral) {
        def res = intercept(stringLiteral){ !hasMethodComment(it) }
        return res
    }

    void setCompilationUnit(CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit
        this.comments = compilationUnit.getCommentList().findAll({ it -> it.toString().contains(methodComment) })
    }

    boolean hasMethodComment(StringLiteral stringLiteral) {
        def parent = stringLiteral.parent
        while (parent != null && !(parent instanceof TypeDeclaration)) {
            if(parent instanceof MethodDeclaration){
                def doc = parent.getJavadoc()
                return doc.toString().contains(methodComment)
            }
            parent = parent.parent
        }
        return false
    }
}
