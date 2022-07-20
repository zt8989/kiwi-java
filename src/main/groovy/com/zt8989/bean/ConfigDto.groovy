package com.zt8989.bean

/**
 * @author zhouteng
 * @Date 2022/4/7 
 */
class ConfigDto {
    private String BAIDU_API_APP_ID;
    private String BAIDU_API_KEY;
    private String i18nImportClass;
    private List<MessageModule> modules
    private List<String> fileExcludes
    private List<String> chineseExcludes
    private List<String> preLoadMessages

    String getBAIDU_API_APP_ID() {
        return BAIDU_API_APP_ID
    }

    void setBAIDU_API_APP_ID(String BAIDU_API_APP_ID) {
        this.BAIDU_API_APP_ID = BAIDU_API_APP_ID
    }

    String getBAIDU_API_KEY() {
        return BAIDU_API_KEY
    }

    void setBAIDU_API_KEY(String BAIDU_API_KEY) {
        this.BAIDU_API_KEY = BAIDU_API_KEY
    }

    String getI18nImportClass() {
        return i18nImportClass
    }

    void setI18nImportClass(String i18nImportClass) {
        this.i18nImportClass = i18nImportClass
    }

    List<MessageModule> getModules() {
        return modules
    }

    void setModules(List<MessageModule> modules) {
        this.modules = modules
    }

    List<String> getFileExcludes() {
        return fileExcludes ?: []
    }

    void setFileExcludes(List<String> fileExcludes) {
        this.fileExcludes = fileExcludes
    }

    List<String> getChineseExcludes() {
        return chineseExcludes ?: []
    }

    void setChineseExcludes(List<String> chineseExcludes) {
        this.chineseExcludes = chineseExcludes
    }

    List<String> getPreLoadMessages() {
        return preLoadMessages ?: []
    }

    void setPreLoadMessages(List<String> preLoadMessages) {
        this.preLoadMessages = preLoadMessages
    }
}
