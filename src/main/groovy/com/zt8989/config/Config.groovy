package com.zt8989.config

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.zt8989.FileWalker
import com.zt8989.Kiwi
import com.zt8989.ResourceLoader
import com.zt8989.bean.ConfigDto
import com.zt8989.bean.MessageModule
import com.zt8989.filter.ConstantFilter
import com.zt8989.filter.GitDiffFilter
import com.zt8989.translator.BaiduTranslator
import com.zt8989.translator.CachedKeyTranslator
import com.zt8989.translator.KeyTranslator
import com.zt8989.translator.MessageSourcesTranslator
import com.zt8989.util.OptionUtils
import groovy.yaml.YamlSlurper
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class Config {
    private static final logger = LoggerFactory.getLogger(Config.class.name)

    static final var DOUBLE_BYTE_REGEX = /[^\x00-\xff]/
    ConfigDto configDto;
    String baseUrl
    String configPath
    String diffRef

    static Config from(String[] args){
        def config = new Config()
        def cmd = OptionUtils.getOptions(args)
        config.baseUrl = cmd.getOptionValue("baseUrl", Paths.get(".").toAbsolutePath().toString())
        logger.debug("baseUrl: ${config.baseUrl}")
        config.configPath = cmd.getOptionValue("config", Paths.get(config.baseUrl, "kiwi.yaml").toAbsolutePath().toString())
        logger.debug("config file: ${config.configPath}")
        if (cmd.hasOption("diffRef")){
            config.diffRef = cmd.getOptionValue("diffRef", "develop")
        }
        config.configDto = config.getYamlConfig()
        return config
    }


    String getI18nImportClass(){
        return configDto.i18nImportClass
    }

    String getI18nClass() {
        return configDto.i18nImportClass.split("\\.").last()
    }

    String getBaiduApiAppId(){
        return configDto.BAIDU_API_APP_ID
    }

    String getBaiduApiKey(){
        return configDto.BAIDU_API_KEY
    }

    List<Kiwi> build(){
        ConfigDto yaml = this.configDto
        List<MessageModule> modules = yaml.modules
        List<String> fileExcludes = yaml.fileExcludes
        def translateList = []
        BiMap<String, String> keyMap = HashBiMap.create()
        for(int i=0;i< modules.size(); i++){
            def module = modules.get(i)
            List<Path> fileLists = getMessageSourceList()
            def translator = new CachedKeyTranslator(
                    new MessageSourcesTranslator(
                            new KeyTranslator(
                                    new BaiduTranslator(this),
                                    yaml.joiner,
                                    yaml.prefix
                            ),
                            fileLists
                    ),
                    keyMap
            )

            def filters = []
            if(diffRef){
                filters.add(GitDiffFilter.make(this))
            }
            filters.addAll(Kiwi.getDefaultFilters(this))
            def path = Paths.get(baseUrl, module.path)
            logger.info("module path: {}", path)
            logger.info("message path: {}", module.messageLocation)
            def resourceLoader = new ResourceLoader(Paths.get(baseUrl, module.messageLocation, "messages_zh_CN.properties").toFile().path)
            def fileWalker = new FileWalker(location: path, excludeList: fileExcludes)
            translateList.add(new Kiwi(
                    translator,
                    resourceLoader,
                    fileWalker,
                    filters,
                    this,
                    keyMap
            ))
        }
        return translateList
    }

    public ConfigDto getYamlConfig() {
        if(configDto){
            return configDto
        } else {
            def ys = new YamlSlurper()
            def yaml = ys.parse(new File(configPath))
            configDto = yaml as ConfigDto
            return configDto
        }
    }

    public List<Path> getMessageSourceList() {
        def fileLists = configDto.preLoadMessages.collectMany {
            def fileWalker = new FileWalker(
                    glob: it,
                    location: baseUrl,
            )
            fileWalker.match()
        }
        fileLists
    }

}
