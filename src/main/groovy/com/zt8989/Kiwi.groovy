package com.zt8989

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.zt8989.base.Task
import com.zt8989.config.Config
import com.zt8989.filter.*
import com.zt8989.transform.*
import com.zt8989.translator.Translator
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.StringLiteral
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jface.text.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class Kiwi {
    private final Logger logger = LoggerFactory.getLogger(Kiwi.class.name)

    public static void main(String[] args) {
        def config = Config.from(args)
        def runners = config.build()
        runners.each {it.run() }
    }

}
