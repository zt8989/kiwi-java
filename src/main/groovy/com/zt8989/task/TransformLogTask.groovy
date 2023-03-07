package com.zt8989.task


import com.zt8989.FileWalker
import com.zt8989.Parser
import com.zt8989.base.Task
import com.zt8989.config.Config
import com.zt8989.log.transform.AbstractTransform
import com.zt8989.log.transform.ConcatLogStringTransform
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jface.text.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Predicate

class TransformLogTask implements Task{
    private final Logger logger = LoggerFactory.getLogger(TransformLogTask.class.name)

    private FileWalker fileWalker;
    private Config config;
    List<Predicate<MethodInvocation>> filters

    TransformLogTask(FileWalker fileWalker, List<Predicate<MethodInvocation>> filters,  Config config){
        this.fileWalker = fileWalker
        this.config = config
        this.filters = filters
    }

    @Override
    void run() {
        def paths = this.fileWalker.match()
        paths.each {path ->
            def file = path.toString()
            def doc = getSource(file)
            if(doc.isPresent()){
                new File(file).withWriter("UTF-8") {
                    it.write(doc.get())
                }
            }
        }
    }

    Optional<String> getSource(String file){
        def src = new File(file)
        def content = src.text
        def parser = new Parser()
        CompilationUnit result = parser.ast(content)
        Document document = new Document(src.text)
        def ast = result.getAST()
        def rewrite = ASTRewrite.create(ast)
        List<AbstractTransform> transforms = [
                new ConcatLogStringTransform(rewrite, result, config)
        ]

        def list = getTransList(result)
        if(list.size() > 0){
            logger.info("processing file: {}", file)
            transforms.forEach({
                list = it.transform(list)
            })
            def editor = rewrite.rewriteAST(document, null)
            editor.apply(document)
            return Optional.of(document.get())
        } else {
            return Optional.empty()
        }
    }

    List<MethodInvocation> visitMethodInvocation(CompilationUnit result){
        var transformList = []
        result.accept(new ASTVisitor(){
            @Override
            boolean visit(MethodInvocation node) {
                if(filters.every(it -> it.test(node))){
                    transformList.add(node)
                }
                return super.visit(node)
            }
        })
        return transformList
    }

    def getTransList(CompilationUnit result){
        var transformList = visitMethodInvocation(result)
        return transformList
    }

}
