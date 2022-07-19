package com.zt8989

import com.zt8989.translator.BaiduTranslator
import spock.lang.Specification


class BaiduTranslatorTest extends Specification{
    BaiduTranslator translator

    void setup(){
        translator = new BaiduTranslator()
    }

    void testTranslate(){
        when:
        def json = translator.translate("已核销")
        println(json)
        List result = json.trans_result
        def first = result.iterator().next()
        then:
        first.dst == "Written off"
    }
}
