package com.zt8989.filter

import com.zt8989.config.Config
import org.eclipse.jdt.core.dom.StringLiteral

import java.util.function.Predicate

abstract class BaseFilter implements Predicate<Tuple2<StringLiteral, String>> {
    Config config

    BaseFilter(Config config) {
        this.config = config
    }
}
