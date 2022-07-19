package com.zt8989.bean

import groovy.transform.ToString

@ToString
class TranslateResultDto {
    String from
    String to
    List<TranslateDto> trans_result
    String error_code
    String error_msg
    Map<String, String> data
}
