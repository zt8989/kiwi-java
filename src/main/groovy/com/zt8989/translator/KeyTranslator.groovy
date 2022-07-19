package com.zt8989.translator

/**
 * @author zhouteng
 * @Date 2022/4/6
 */
class KeyTranslator implements Translator{
    private Translator translator;
    private final String EN_EXP = /[a-zA-Z]+/
    Integer maxLength = 50
    Integer maxKeyWordCount = 10

    KeyTranslator(Translator translator) {
        this.translator = translator;
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
        while (newKeys.size() > maxKeyWordCount &&  newKeys.join("_").length() > maxLength){
            newKeys.remove(newKeys.size() - 1)
        }
        return newKeys.join("_")
    }

    Optional<String> translate(String cn) {
        def keys = translateKeys(cn)
        if(keys.size() > 0){
            def key = getKey(keys)
            return Optional.of(key)
        }
        throw new Error("no translate key found for " + cn)
    }
}
