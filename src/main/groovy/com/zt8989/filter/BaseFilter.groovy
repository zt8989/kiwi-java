package com.zt8989.filter

import com.zt8989.config.Config

abstract class BaseFilter {
    Config config

    BaseFilter(Config config) {
        this.config = config
    }
}
