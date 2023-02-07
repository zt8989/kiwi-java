package com.zt8989.git

import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Paths;

public class GitCommandTest extends Specification{

    @Ignore
    def "git command test"(){
        given:
        def dir = Paths.get(".")
        when:
            def content
            Git.runCommand(dir, a -> content = a, a -> {}, "git", "diff", "develop", "head")
        then:
        content != null
    }
}
