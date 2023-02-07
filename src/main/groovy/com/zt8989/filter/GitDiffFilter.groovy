package com.zt8989.filter

import com.google.common.base.Strings
import com.zt8989.config.Config
import com.zt8989.git.DiffParser
import com.zt8989.git.Git
import com.zt8989.git.Info
import com.zt8989.git.TypeEnum
import org.codehaus.groovy.util.StringUtil
import org.eclipse.jdt.core.dom.StringLiteral
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Predicate


class GitDiffFilter extends BaseFilter  {
    private static final Logger logger = LoggerFactory.getLogger(GitDiffFilter.class.name)
    private List<Info> infoList;

    static GitDiffFilter make(Config config){
        def dir = Paths.get(config.baseUrl)
        String content = null
        Git.runCommand(dir, c -> content = c, c -> {}, "git", "diff", config.diffRef, "head")
        if(Strings.isNullOrEmpty(content)){
            throw new Exception("git has no diff")
        }
        def parser = new DiffParser()
        def infoList = parser.parse(content).findAll({ it.type == TypeEnum.modify.type || it.type == TypeEnum.add.type })
        return new GitDiffFilter(config, infoList)
    }

    GitDiffFilter(Config config, List<Info> infoList) {
        super(config)
        this.infoList = infoList
    }

    boolean intercept(StringLiteral stringLiteral, Closure<StringLiteral> closure){
        def res = closure.call(stringLiteral)
        if(!res){
            logger.debug("过滤: {}", stringLiteral.literalValue)
            logger.debug("过滤原因: git增量过滤" )
        }
        return res
    }

    @Override
    boolean test(Tuple2<StringLiteral, String> node) {
        def res = intercept(node.getV1()) {
            !notFindInGit (node)
        }
        return res
    }

    /**
     * 返回true则需要过滤
     * @param node
     * @return
     */
    boolean notFindInGit(Tuple2<StringLiteral, String> node){
        List<Info> list = infoList.findAll({ it -> node.v2.contains(it.newPath) })
        if(list.size() == 0){
            return true
        }
        if(list[0].type == "add"){
            return false
        }
        if(list.any { it -> it.any { info -> info.hunks.any{
        hunks -> hunks.changes.any {
            it.type == "insert" && it.content.contains(node.v1.literalValue) } } }}) {
            return false
        }
        return true
    }
}
