package com.zt8989.translator

import com.google.common.collect.Sets
import com.zt8989.exception.NoTranslateFoundException

/**
 * @author zhouteng
 * @Date 2022/4/6
 */
class KeyTranslator implements Translator{
    private Translator translator;
    private final String EN_EXP = /[a-zA-Z]+/
    Integer maxLength = 50
    Integer maxKeyWordCount = 10
    String joiner
    String prefix
    Set<String> sameTranslateSet;

    KeyTranslator(Translator translator, String joiner, String prefix) {
        this.translator = translator;
        this.joiner = joiner
        this.prefix = prefix
        this.sameTranslateSet = Sets.newHashSet()
    }

    List<String> translateKeys(String cn){
        def translateEn = translator.translate(cn)
        if(translateEn.isPresent()){
            def en = translateEn.get()
            def enKey = (en =~ EN_EXP).findAll().collect({((String)it).toLowerCase()})
            return enKey
        }
        return []
    }

    String getKey(List<String> keys){
        def newKeys = keys.collect()
        while (newKeys.size() > maxKeyWordCount &&  newKeys.join(joiner).length() > maxLength){
            newKeys.remove(newKeys.size() - 1)
        }
        return prefix + newKeys.join(joiner)
    }

    Optional<String> translate(String cn) {
        if(!sameTranslateSet.contains(cn)){
            def keys = translateKeys(cn)
            if(keys.size() > 0){
                def key = getKey(keys)
                return Optional.of(key)
            } else {
                sameTranslateSet.add(cn)
            }
        }
        throw new NoTranslateFoundException("no translate key found for " + cn)
    }
}
