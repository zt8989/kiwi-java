package com.zt8989

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
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

import java.util.function.Predicate

public class Kiwi {
    private final Logger logger = LoggerFactory.getLogger(Kiwi.class.name)

    public static void main(String[] args) {
        def config = Config.from(args)
        def runners = config.build()
        runners.each {it.run() }
    }

    private List<Predicate<StringLiteral>> filters = []
    private List<AbstractTransform> transforms = []
    private Translator translator;
    private ResourceLoader resourceLoader;
    private FileWalker fileWalker;
    private Config config;
    private BiMap<String, String> keyMap;

    static List<Predicate<StringLiteral>> getDefaultFilters(Config config){
       return [new LogInfoFilter(config), new AnnotationFilter(config), new RegexpFilter(config),
               new I18nFilter(config), new EnumFilter(config),
               new StringEqualsFilter(config), new MainFilter(config),
               new ConstantFilter(config)
       ]
    }

    Kiwi(Translator translator, ResourceLoader resourceLoader, FileWalker fileWalker, List<Predicate<StringLiteral>> filters, Config config, BiMap<String, String> keyMap){
        this.translator = translator
        this.resourceLoader = resourceLoader
        this.fileWalker = fileWalker
        this.filters = Optional.ofNullable(filters).orElse(getDefaultFilters(config))
        this.config = config
        this.keyMap = keyMap
    }

    void run(){
        def paths = this.fileWalker.match()
        keyMap.putAll(HashBiMap.create(resourceLoader.loadProperties()).inverse())
        def filter = this.filters.find(it -> it instanceof LogInfoFilter) as LogInfoFilter
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
        def filters = this.filters.collect()
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

    def getTransList(Parser parser, CompilationUnit result, List<Predicate<StringLiteral>> filters, String file){
        var originTranslateList = parser.visitChineseText(result)
        var translateList = originTranslateList.collect()
        filters.forEach(filter -> {
            translateList = translateList.findAll{ filter.test(it) }
        })
        if(originTranslateList.size() != translateList.size()){
            logger.info(file)
            logger.info("filtered {} strings", (originTranslateList.size() - translateList.size()))
        }
        return translateList
    }
}
