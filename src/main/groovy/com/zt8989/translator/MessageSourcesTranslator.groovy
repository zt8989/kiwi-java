package com.zt8989.translator


import com.google.common.collect.HashBiMap
import com.zt8989.util.MessageSourceUtils

import java.nio.file.Path

/**
 * @author zhouteng
 * @Date 2022/4/7
 */
class MessageSourcesTranslator implements Translator{
    Map<String,String> map
    Translator translator

    MessageSourcesTranslator(Translator translator, List<Path> files){
        this.translator = translator;
        map = HashBiMap.create(MessageSourceUtils.getMessageMap(files)).inverse()
    }

    @Override
    Optional<String> translate(String cn) {
        if(map.containsKey(cn)){
            println("[${MessageSourcesTranslator.class.name}] query: " + cn)
            def result = map.get(cn)
            println("[${MessageSourcesTranslator.class.name}] result: " + result)
            return Optional.of(result)
        }
        return translator.translate(cn)
    }
}
