package com.zt8989

import com.zt8989.config.Config
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.*

class Parser {

    CompilationUnit ast(String content){
        var parser = ASTParser.newParser(AST.JLS8)
        parser.setSource(content.toCharArray())
        var options = JavaCore.options
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
        parser.compilerOptions = options
        var result = parser.createAST(null)
        return result
    }

    List<StringLiteral> visitImport(CompilationUnit result, Config config){
        var list = []
        result.accept(new ASTVisitor(){
            @Override
            boolean visit(ImportDeclaration node) {
                if(node.getName().fullyQualifiedName == config.i18nImportClass){
                    list.add(node.getName())
                }
                return super.visit(node)
            }
        })
        return list
    }

    List<StringLiteral> visitChineseText(CompilationUnit result){
        var translateList = []
        result.accept(new ASTVisitor(){
            @Override
            boolean visit(StringLiteral node) {
                if(node.toString() =~ Config.DOUBLE_BYTE_REGEX){
                    translateList.add(node)
                }
                return super.visit(node)
            }
        })
        return translateList
    }
}
