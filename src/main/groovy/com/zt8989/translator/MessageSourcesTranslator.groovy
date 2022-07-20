package com.zt8989.translator


import com.google.common.collect.HashBiMap
import com.zt8989.util.MessageSourceUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

/**
 * @author zhouteng
 * @Date 2022/4/7
 */
class MessageSourcesTranslator implements Translator{
    private final Logger logger = LoggerFactory.getLogger(MessageSourcesTranslator.class.name)
    Map<String,String> map
    Translator translator

    MessageSourcesTranslator(Translator translator, List<Path> files){
        this.translator = translator;
        map = HashBiMap.create(MessageSourceUtils.getMessageMap(files)).inverse()
    }

    @Override
    Optional<String> translate(String cn) {
        if(map.containsKey(cn)){
            logger.info("query: {}", cn)
            def result = map.get(cn)
            logger.info("result: {}", result)
            return Optional.of(result)
        }
        return translator.translate(cn)
    }
}
