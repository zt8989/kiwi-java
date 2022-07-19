package com.zt8989


import spock.lang.Specification
/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ResourceLoaderTest extends Specification{
    ResourceLoader resourceLoader

    def setup(){
        resourceLoader = new ResourceLoader()
    }

    def "given file when load properties then return properties"(){
        given:
            def file = "/test.properties"
            def realFile = getClass().getResource(file).toURI().path
        when:
            var properties = resourceLoader.loadProperties(realFile)
        then:
            properties.get("abc") == "你好，世界"
    }

    def "given properties when save properties then return properties"(){
        given:
        def file = "/test1.properties"
        def realFile = getClass().getResource(file).toURI().path
        def map = ["abc": "你好，世界1"]
        when:
        resourceLoader.saveProperties(realFile, map)
        var properties = resourceLoader.loadProperties(realFile)
        then:
        properties.get("abc") == "你好，世界1"
    }
}
