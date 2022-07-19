package com.zt8989.filter


import spock.lang.Specification

/**
 * @author zhouteng
 * @Date 2022/4/6
 */
class RegexpFilterTest extends Specification {
    def "given string when filter then true"(){
        given:
            def str = "{0}“{1}”"
            def filter = new RegexpFilter()
        when:
            def result = str.matches(filter.REGEXP)
        then:
            result
    }
}
