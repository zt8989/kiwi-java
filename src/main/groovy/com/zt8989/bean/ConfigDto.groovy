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
    boolean fieldTranslate = true
}
