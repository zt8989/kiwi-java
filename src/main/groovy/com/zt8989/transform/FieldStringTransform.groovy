package com.zt8989.transform

import com.zt8989.config.Config
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
/**
 * @author zhouteng
 * @Date 2022/4/2
 */
public class FieldStringTransform extends AbstractTransform {

    FieldStringTransform(ASTRewrite astRewrite, CompilationUnit compilationUnit, Translator translator, Config config) {
        super(astRewrite, compilationUnit, translator, config)
    }

    Optional<FieldDeclaration> isFieldDeclaration(StringLiteral stringLiteral){
        def parent = stringLiteral.getParent();
        while (parent != null && !(parent instanceof TypeDeclaration)){
            if(parent instanceof FieldDeclaration){
                return Optional.of((FieldDeclaration)parent);
            }
            parent = parent.getParent();
        }
        return Optional.empty();
    }

    void transformFieldToGetMethod(FieldDeclaration fieldDeclaration, StringLiteral stringLiteral){
        def method = ast.newMethodDeclaration();
        def variable = (VariableDeclarationFragment)fieldDeclaration.fragments()[0]
        def variableName = ((SimpleName)variable.getName()).identifier
        def methodName = "get" + variableName
        method.setName(ast.newSimpleName(methodName));
        def modifiers = fieldDeclaration.modifiers().findAll {
            it -> ((Modifier)it).private || ((Modifier)it).public || ((Modifier)it).protected
        }.collect({ ASTNode.copySubtree(fieldDeclaration.getAST(), it) })
        method.modifiers().addAll(modifiers);
        method.setReturnType2(ASTNode.copySubtree(fieldDeclaration.getAST(), fieldDeclaration.getType()));
        def body = ast.newBlock();
        method.setBody(body);
        def returnType = ast.newReturnStatement()
        body.statements().add(returnType);
        StringLiteral node = ASTNode.copySubtree(stringLiteral.getAST(), stringLiteral)
        translateKey(node)
        returnType.expression = getI18nCall([node])
        astRewrite.replace(fieldDeclaration, method, null)

        replaceFieldWithMethodInvocation(variableName, methodName)
    }

    List<SimpleName> getAllSimpleName(CompilationUnit result, String identifier){
        var list = []
        result.accept(new ASTVisitor(){
            @Override
            boolean visit(SimpleName node) {
                if(node.identifier == identifier){
                    list.add(node)
                }
                return super.visit(node)
            }
        })
        return list
    }

    void replaceFieldWithMethodInvocation(String identifier, String methodName){
        def list = getAllSimpleName(compilationUnit, identifier)
        def method = ast.newMethodInvocation()
        method.setName(ast.newSimpleName(methodName))
        list.each {astRewrite.replace(it, method, null)}
    }

    public List<StringLiteral> transform(List<StringLiteral> stringLiterals) {
        def cloneList = stringLiterals.collect()
        for(stringLiteral in stringLiterals){
            def field = isFieldDeclaration(stringLiteral);
            if(field.isPresent()){
                if(config.configDto.fieldTranslate){
                    transformFieldToGetMethod(field.get(), stringLiteral);
                }
                cloneList.remove(stringLiteral);
            }
        }
        return cloneList;
    }
}
