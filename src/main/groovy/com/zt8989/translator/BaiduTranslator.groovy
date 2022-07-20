package com.zt8989.translator

import com.zt8989.bean.TranslateResultDto
import com.zt8989.config.Config
import groovy.json.JsonSlurper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class BaiduTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(BaiduTranslator.class.name)
    final String url = "https://fanyi-api.baidu.com/api/trans/vip/translate"
    final String from = 'zh'
    final String to = 'en'

    private OkHttpClient okHttpClient;
    private JsonSlurper jsonSlurper
    Config config

    BaiduTranslator(Config config){
        this.config = config
        okHttpClient = new OkHttpClient()
        jsonSlurper = new JsonSlurper()
    }

    Optional<String> translate(String cn){
        def salt = new Date()
        def body = new FormBody.Builder()
            .add("q", cn)
            .add("from", from)
            .add("to", to)
            .add("appid", config.baiduApiAppId)
            .add("salt", String.valueOf(salt.time))
            .add("sign", (config.baiduApiAppId + cn + salt.time + config.baiduApiKey).md5())
            .build()
        def request = new Request.Builder()
            .url(url)
            .post(body)
            .build()
        logger.info("query: {}", cn)
        def response
        try{
            response = okHttpClient.newCall(request).execute()
            def json = jsonSlurper.parse(response.body().bytes())
            logger.info("result: {}", json)
            def translate = new TranslateResultDto(json)
            response.close()
            return Optional.ofNullable(translate).map(it -> it.trans_result)
                    .map(it -> it[0]).map(it -> it.dst)
        } catch(SocketTimeoutException e) {
            response?.close()
            return Optional.empty()
        }
    }
}
