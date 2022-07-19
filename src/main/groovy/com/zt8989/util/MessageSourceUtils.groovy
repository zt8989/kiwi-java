package com.zt8989.util


import java.nio.file.Path

/**
 * @author zhouteng
 * @Date 2022/4/7
 */
class MessageSourceUtils {
    static Map<String, String> getMessageMap(List<Path> files) {
        def map = [:]
        files.each {
            new File(it.toString()).withReader("UTF-8") {buffer
                -> {
                    def properties = new Properties()
                    properties.load(buffer)
                    map.putAll(properties)
                }
            }
        }
        return map
    }
}
