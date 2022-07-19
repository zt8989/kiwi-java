package com.zt8989.bean

/**
 * @author zhouteng
 *  @Date 2022/4/11
 */
class MuteInt {
    int ref

    def next(){
        ref += 1
        this
    }

    String toString(){
        return ref.toString()
    }
}
