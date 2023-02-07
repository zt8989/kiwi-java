package com.zt8989.git

import spock.lang.Specification

class DiffParserTest extends Specification{

    def "should have type add"(){
        given:
        def parser = new DiffParser()
        when:
        def info = parser.parse(getClass().getResource("/git/add.diff").text)
        Info file = info[0]
        then:
        file.type == 'add'
        file.oldPath == '/dev/null'
        file.newPath == 'a.txt'
        file.newMode == '100644'
    }

    def "should have type delete"(){
        given:
        def parser = new DiffParser()
        when:
        def info = parser.parse(getClass().getResource("/git/rm.diff").text)
        Info file = info[0]
        then:
        file.type == 'delete'
        file.oldPath == 'a.txt'
        file.oldMode == '100644'
        file.newPath == '/dev/null'
    }

    def "should have type rename"(){
        given:
        def parser = new DiffParser()
        when:
        def info = parser.parse(getClass().getResource("/git/mv.diff").text)
        Info file = info[0]
        then:
        file.type == 'rename'
        file.oldPath == 'b.txt'
        file.newPath == 'c.txt'
    }

    def "should have type modify"(){
        given:
            def parser = new DiffParser()
        when:
            def info = parser.parse(getClass().getResource("/git/edit.diff").text)
            Info file = info[0]
        then:
            file.type == 'modify'
            file.oldPath == 'a.txt'
            file.newPath == 'a.txt'
            file.oldMode == '100644'
            file.newMode == '100644'
    }

    def "should parse filename correctly if whitespace included"(){
        given:
        def parser = new DiffParser()
        when:
        def info = parser.parse(getClass().getResource("/git/edit-ws.diff").text)
        Info file = info[0]
        then:
        file.type == 'modify'
        file.oldPath == 'a b/a.txt'
        file.newPath == 'a b/a.txt'
    }
}
