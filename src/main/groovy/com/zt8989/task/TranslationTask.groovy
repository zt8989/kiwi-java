package com.zt8989.task

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.zt8989.FileWalker
import com.zt8989.Parser
import com.zt8989.ResourceLoader
import com.zt8989.base.Task
import com.zt8989.config.Config
import com.zt8989.filter.*
import com.zt8989.transform.*
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jface.text.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class TranslationTask implements Task{
    private final Logger logger = LoggerFactory.getLogger(TranslationTask.class.name)

    private List<BaseFilter> filters = []
    private List<AbstractTransform> transforms = []
    private Translator translator;
    private ResourceLoader resourceLoader;
    private FileWalker fileWalker;
    private Config config;
    private BiMap<String, String> keyMap;

    static List<BaseFilter> getDefaultFilters(Config config){
       return [new FunctionCallFilter(config), new AnnotationFilter(config), new RegexpFilter(config),
               new I18nFilter(config), new EnumFilter(config),
               new StringEqualsFilter(config), new MainFilter(config),
               new ConstantFilter(config),
               new FieldStringFilter(config)
       ]
    }

    TranslationTask(Translator translator, ResourceLoader resourceLoader, FileWalker fileWalker, List<BaseFilter> filters, Config config, BiMap<String, String> keyMap){
        this.translator = translator
        this.resourceLoader = resourceLoader
        this.fileWalker = fileWalker
        this.filters = Optional.ofNullable(filters).orElse(getDefaultFilters(config))
        this.config = config
        this.keyMap = keyMap
    }

    void run(){
        def paths = this.fileWalker.match()
        try {

            def inverseMap = HashBiMap.create(resourceLoader.loadProperties()).inverse()
            inverseMap.entrySet().forEach { keyMap.forcePut(it.key, it.value) }
        } catch(IllegalArgumentException e){
            throw new RuntimeException("预加载中是否包含相同的key, 且对应的中文不一致", e)
        }
        def filter = this.filters.find(it -> it instanceof FunctionCallFilter) as FunctionCallFilter
        if(filter != null){
            filter.listeners.add({ StringLiteral it ->
                if(keyMap.containsKey(it.literalValue)){
                    logger.info("find key: {}", it.literalValue)
                    keyMap.remove(it.literalValue)
                }
            })
        }
        paths.each {path ->
            def file = path.toString()
            def doc = getSource(file)
            if(doc.isPresent()){
                new File(file).withWriter("UTF-8") {
                    it.write(doc.get())
                }
                resourceLoader.saveProperties(keyMap.inverse())
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
        transforms = [
                new ConcatStringTransform(rewrite, result, translator, config),
                new MessageFormatTransform(rewrite, result, translator, config),
                new AnnotationTransform(rewrite, result, translator, config),
                new FieldStringTransform(rewrite, result, translator, config),
                new StringTransform(rewrite, result, translator, config),
        ]
        def filters = []

        filters.addAll(this.filters.collect())
        filters.add(new MethodFilter(config, result))

        def list = getTransList(parser, result, filters, file)
        if(list.size() > 0){
            logger.info("processing file: {}", file)
            appendImport(parser, result, rewrite)
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

     def appendImport(Parser parser, CompilationUnit unit, ASTRewrite rewrite){
        def ast = unit.getAST()
        def list = parser.visitImport(unit, config)
        if(list.size() == 0){
            def importDeclaration = ast.newImportDeclaration()
            importDeclaration.setName(ast.newName(config.i18nImportClass))
            rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY).insertLast(importDeclaration, null)
        }
    }

    def getTransList(Parser parser, CompilationUnit result, List<BaseFilter> filters, String file){
        var originTranslateList = parser.visitChineseText(result)
        var translateList = originTranslateList.collect()
        filters.forEach(filter -> {
            translateList = translateList.findAll{ filter.test(new Tuple2(it, file)) }
        })
        if(originTranslateList.size() != translateList.size()){
            logger.info(file)
            logger.info("filtered {} strings", (originTranslateList.size() - translateList.size()))
        }
        return translateList
    }
}
