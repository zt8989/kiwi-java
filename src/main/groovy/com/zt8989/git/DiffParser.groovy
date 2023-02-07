package com.zt8989.git

public class DiffParser {
    public static final int STAT_START = 2;
    public static final int STAT_FILE_META = 3;
    public static final int STAT_HUNK = 5;

    def parseInt(String number, Integer radix){
        def p = ~/\d+/
        def matcher = p.matcher(number)
        if(matcher.find()){
            return Integer.valueOf(matcher.group())
        }
        return 0
    }

    List<Info> parse(String source){
        var infos = [];
        var stat = STAT_START;
        Info currentInfo;
        Hunk currentHunk;
        var changeOldLine;
        var changeNewLine;

        var lines = source.split('\n');
        var linesLen = lines.length;
        var i = 0;

        while (i < linesLen) {
            var line = lines[i];

            if (line.indexOf('diff --git') == 0) {
                // read file
                currentInfo = new Info([
                    hunks: [],
                    oldEndingNewLine: true,
                    newEndingNewLine: true
                ]);

                infos.push(currentInfo);


                // 1. 如果oldPath是/dev/null就是add
                // 2. 如果newPath是/dev/null就是delete
                // 3. 如果有 rename from foo.js 这样的就是rename
                // 4. 如果有 copy from foo.js 这样的就是copy
                // 5. 其它情况是modify
                String currentInfoType = null;


                // read type and index
                String simiLine;
                simiLoop: while (++i < lines.size() && (simiLine = lines[i])) {
                    var spaceIndex = simiLine.indexOf(' ');
                    var infoType = spaceIndex > -1 ? simiLine.substring(0, spaceIndex) : 'modify';

                    switch (infoType) {
                        case 'diff': // diff --git
                            i--;
                            break simiLoop;

                        case 'deleted':
                        case 'new':
                            var leftStr = simiLine.substring(spaceIndex + 1);
                            if (leftStr.indexOf('file mode') == 0) {
                                currentInfo[infoType == 'new' ? 'newMode' : 'oldMode'] = leftStr.substring(10);
                            }
                            break;

                        case 'similarity':
                            currentInfo.similarity = parseInt(simiLine.split(' ')[2], 10);
                            break;

                        case 'index':
                            var segs = simiLine.substring(spaceIndex + 1).split(' ');
                            var revs = segs[0].split('\\.\\.');
                            currentInfo.oldRevision = revs[0];
                            currentInfo.newRevision = revs[1];

                            if (segs.size() > 1) {
                                currentInfo.oldMode = currentInfo.newMode = segs[1];
                            }
                            break;


                        case 'copy':
                        case 'rename':
                            var infoStr = simiLine.substring(spaceIndex + 1);
                            if (infoStr.indexOf('from') == 0) {
                                currentInfo.oldPath = infoStr.substring(5);
                            }
                            else { // rename to
                                currentInfo.newPath = infoStr.substring(3);
                            }
                            currentInfoType = infoType;
                            break;

                        case '---':
                            var oldPath = simiLine.substring(spaceIndex + 1);
                            var newPath = lines[++i].substring(4); // next line must be "+++ xxx"
                            if (oldPath == '/dev/null') {
                                newPath = newPath.substring(2);
                                currentInfoType = 'add';
                            }
                            else if (newPath == '/dev/null') {
                                oldPath = oldPath.substring(2);
                                currentInfoType = 'delete';
                            } else {
                                currentInfoType = 'modify';
                                oldPath = oldPath.substring(2);
                                newPath = newPath.substring(2);
                            }

                            currentInfo.oldPath = oldPath;
                            currentInfo.newPath = newPath;
                            stat = STAT_HUNK;
                            break simiLoop;
                    }
                }

                currentInfo.type = currentInfoType ?: 'modify';
            }
            else if (line.indexOf('Binary') == 0) {
                currentInfo.isBinary = true;
                currentInfo.type = line.indexOf('/dev/null and') >= 0
                        ? 'add'
                        : (line.indexOf('and /dev/null') >= 0 ? 'delete' : 'modify');
                stat = STAT_START;
                currentInfo = null;
            }
            else if (stat == STAT_HUNK) {
                if (line.indexOf('@@') == 0) {
                    def pattern = ~/^@@\s+-([0-9]+)(,([0-9]+))?\s+\+([0-9]+)(,([0-9]+))?/
                    var match = pattern.matcher(line)
                    if(!match.find()){
                        throw new Exception("parse error, " + line)
                    }
                    currentHunk = new Hunk([
                        content: line,
                        oldStart: Integer.valueOf(match.group(1)),
                        newStart: Integer.valueOf(match.group(4)),
                        oldLines: match.group(3) ? Integer.valueOf(match.group(3)) : 1,
                        newLines: match.group(6) ? Integer.valueOf(match.group(6)) : 1,
                        changes: []
                    ]);

                    currentInfo.hunks.push(currentHunk);
                    changeOldLine = currentHunk.oldStart;
                    changeNewLine = currentHunk.newStart;
                }
                else {
                    var typeChar = line.substring(0, 1);
                    var change = new Change([
                        content: line.substring(1)
                    ]);

                    switch (typeChar) {
                        case '+':
                            change.type = 'insert';
                            change.isInsert = true;
                            change.lineNumber = changeNewLine;
                            changeNewLine++;
                            break;

                        case '-':
                            change.type = 'delete';
                            change.isDelete = true;
                            change.lineNumber = changeOldLine;
                            changeOldLine++;
                            break;

                        case ' ':
                            change.type = 'normal';
                            change.isNormal = true;
                            change.oldLineNumber = changeOldLine;
                            change.newLineNumber = changeNewLine;
                            changeOldLine++;
                            changeNewLine++;
                            break;

                        case '\\': // Seems "no newline" is the only case starting with /
                            var lastChange = currentHunk.changes[currentHunk.changes.size() - 1];
                            if (!lastChange.isDelete) {
                                currentInfo.newEndingNewLine = false;
                            }
                            if (!lastChange.isInsert) {
                                currentInfo.oldEndingNewLine = false;
                            }
                    }

                    change.type && currentHunk.changes.push(change);
                }
            }

            i++;
        }

        return infos;
    }
}
