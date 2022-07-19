package com.zt8989.util


import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jdt.core.dom.MethodInvocation
import org.eclipse.jdt.core.dom.SimpleName
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.TypeDeclaration

/**
 * @author zhouteng
 * @Date 2022/4/6
 */
class AstUtils {
    static Optional<MethodInvocation> lookupCallerName(StringLiteral stringLiteral, Closure closure) {
        def optMethodInvocationNode = lookupUntilASTNode(stringLiteral, MethodInvocation.class)
        if(optMethodInvocationNode.isPresent()) {
            MethodInvocation methodInvocation = optMethodInvocationNode.get()
            def methodName = methodInvocation.getExpression()
            if (methodName instanceof SimpleName) {
                def simpleName = (SimpleName) methodName
                if (closure.call(simpleName.getIdentifier())) {
                    return Optional.of(methodInvocation)
                }
            }
        }
        return Optional.empty()
    }

    static def createType(AST ast, String typeName) {
        def typeDeclaration = ast.newTypeDeclaration()
        typeDeclaration.setName(ast.newSimpleName(typeName))
        return typeDeclaration
    }

    static <T extends ASTNode> Optional<T> lookupUntilASTNode(StringLiteral stringLiteral, Class<T> clazz) {
        def parent = stringLiteral.getParent()
        while (parent != null && (!(parent instanceof TypeDeclaration))) {
            if (clazz.isInstance(parent)) {
                return Optional.of(parent as T)
            }
            parent = parent.getParent()
        }
        return Optional.empty()
    }
}