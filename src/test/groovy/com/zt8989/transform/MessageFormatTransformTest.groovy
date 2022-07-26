package com.zt8989.transform

import spock.lang.Specification

class MessageFormatTransformTest extends Specification {
    MessageFormatTransform messageFormatTransform = new MessageFormatTransform(null, null, null, null)

    def "test transform messageFormat to I18n"(){
        given:
            def message = "{} {} {}"
        when:
            def result = messageFormatTransform.replaceMessageFormatWithI18nFormat(message)
        then:
            result == "{0} {1} {2}"
    }
}
