package com.zt8989

/**
 * @author zhouteng
 * @Date 2022/4/1
 */
class ResourceLoader {
    String file;
    ResourceLoader(String file) {
        this.file = file;
    }

    Map<String, String> loadProperties(){
        Properties properties = new Properties()
        def file = new File(file)
        if(file.exists()){
            file.withReader("UTF-8"){
                properties.load(it)
            }
        } else {
            file.write("")
        }
        return properties
    }

    void saveProperties(Map<String, String> map){
        Properties properties = new Properties()
        properties.putAll(map)
        new File(file).withWriter("UTF-8"){
            properties.store(it, "")
        }
    }
}
