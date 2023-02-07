package com.zt8989.git;

import java.util.List;

public class Info {
    List<Hunk> hunks;
    Boolean oldEndingNewLine;
    Boolean newEndingNewLine;

    String type;

    String newMode;
    String oldMode;
    String oldRevision;
    String newRevision;
    String oldPath;
    String newPath;
    Integer similarity;
    Boolean isBinary;
    Boolean isDelete
    Boolean isInsert
}
