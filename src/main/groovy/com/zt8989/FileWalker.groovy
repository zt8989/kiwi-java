package com.zt8989

import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class FileWalker {
    String glob = "glob:**/src/main/java/**/*.java"
    String location
    List<String> excludeList = []

    FileWalker(){
    }

    List<Path> match(){
        def pathMatcher = FileSystems.getDefault().getPathMatcher(
                glob);
        def excludePathMatcher = excludeList.collect {FileSystems.getDefault().getPathMatcher(it)}
        def files = []
        Files.walkFileTree(Paths.get(location), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path,
                                             BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path) && !excludePathMatcher.any {it.matches(path)}) {
                    files.add(path)
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                    throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return files
    }
}
