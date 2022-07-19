package com.zt8989.translator

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * @author zhouteng
 * @Date 2022/4/2
 */
class CachedKeyTranslator implements Translator{
    BiMap<String, String> keyMap
    Translator translator

    CachedKeyTranslator(Translator translator, BiMap<String, String> keyMap){
        this.translator = translator
        this.keyMap = keyMap
    }

    @Override
    Optional<String> translate(String cn) {
        if(keyMap.containsKey(cn)){
            return Optional.of(keyMap.get(cn))
        }
        def key = translator.translate(cn)
        if(key.isPresent()){
            def reverseMap = keyMap.inverse()
            def calcKey = key.get()
            if (!(reverseMap.containsKey(calcKey) && reverseMap.get(calcKey) == cn)) {
                def count = 1
                while (reverseMap.containsKey(calcKey)){
                    calcKey = key.get() + "_" + count
                    count += 1
                }
            }
            keyMap.put(cn, calcKey)
            return Optional.of(calcKey)
        }
        return Optional.empty()
    }
}
