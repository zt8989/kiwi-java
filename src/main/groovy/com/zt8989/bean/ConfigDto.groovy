package com.zt8989.bean

/**
 * @author zhouteng
 * @Date 2022/4/7 
 */
class ConfigDto {
    String BAIDU_API_APP_ID = "";
    String BAIDU_API_KEY = "";
    String i18nImportClass = "";
    List<MessageModule> modules = []
    List<String> fileExcludes = []
    List<String> chineseExcludes = []
    List<String> preLoadMessages = []
    boolean fieldTranslate = false
    String joiner;
    String prefix;
    List<String> functionCallExcludes = []

    List<String> getFunctionCallExcludes(){
        return functionCallExcludes ?:[
                "log.*",
                "logger.*"
        ]
    }

    String getJoiner(){
        return joiner?:"_"
    }

    String getPrefix(){
        return prefix?:""
    }

    boolean getFieldTranslate(){
        return fieldTranslate?:false
    }

    List<String> getChineseExcludes(){
        return chineseExcludes?:[]
    }

    List<String> getFileExcludes(){
        return fileExcludes?:[]
    }

    List<String> getPreLoadMessages(){
        return preLoadMessages?:[]
    }
}
