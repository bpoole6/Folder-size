package com.bpoole6;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FileSystemSize {

    // Where to begin walking in File system
    private static final String ROOT_FOLDER = "E:\\java_libraries";

    // File Depth for walking file system
    private static final int MAX_DEPTH = 4;

    private static final Map<String, FolderDescription> FOLDER_MAP = new HashMap<>();

    private static final char FILE_SEPARATOR = Optional.ofNullable(System.getProperty("file.separator")).map(s->s.charAt(0)).orElse('/');

    public static long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static long getFolderSize(Path folder) {
        if (!Files.isReadable(folder))
            return 0;
        return list(folder).filter(Files::exists).map(a -> {
            // If we've already calculated the size of folder no need to do it again. This will save on time
            if (FOLDER_MAP.containsKey(a.toString())) {
                return FOLDER_MAP.get(a.toString()).getSize();
            } else if (Files.isDirectory(a)) {
                return getFolderSize(a);
            } else {

                return size(a);
            }
        }).reduce(0L, Long::sum);
    }

    public static FolderDescription size(String folder) {
        System.out.println("Working on folder " + folder);
        long size = getFolderSize(Paths.get(folder));
        return FOLDER_MAP.put(folder, new FolderDescription(folder, size));
    }

    public static List<String> walkFileTree() throws IOException {
        List<String> paths = new ArrayList<>();
        /**
         * The reason why {@link Files#walkFileTree(Path, Set, int, FileVisitor)} was used instead of {@link Files#walk(Path, FileVisitOption...)}
         * was because if {@link Files#walk(Path, FileVisitOption...)} encountered a folder or file it couldn't read
         * because of permissions issues it would throw an exception
         */
        Files.walkFileTree(Paths.get(ROOT_FOLDER),
                new HashSet<>(Arrays.asList(FileVisitOption.FOLLOW_LINKS)),
                MAX_DEPTH,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        paths.add(dir.toString());

                        return super.postVisitDirectory(dir, exc);
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                });
        return paths;
    }

    // Driver Code
    public static void main(String[] args) throws IOException{
        List<String> paths = walkFileTree();

        // We are partition the list based off the file system depth. We want to process the deepest folders first so
        // that when we calculate the size of the parent folder we won't have to calculate the size the child folder again.
        Map<Long, List<String>> folderMapByDeepth = paths.stream()
                .collect(Collectors.groupingBy((String a) -> a.chars().filter(c -> c == FILE_SEPARATOR).count()));

        // To speed up processing use multiple threads and process all the folder paths at the same depth.
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        folderMapByDeepth.keySet().stream().sorted(Comparator.comparingLong((Long a) -> a).reversed()).forEach(key -> {
            try {
                customThreadPool.submit(() -> folderMapByDeepth.get(key).parallelStream().map(FileSystemSize::size).collect(Collectors.toList())).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        customThreadPool.shutdownNow();

        //Assign parent/child relation
        FOLDER_MAP.forEach((folder, desc) -> {
            FolderDescription parent = FOLDER_MAP.get(Paths.get(folder).getParent().toString());
            if (parent != null) {
                parent.getChildren().add(desc);
                desc.setParent(parent);
            }
        });

        ObjectMapper mapper = new ObjectMapper();

        //find root most folder
        Optional<FolderDescription> root = FOLDER_MAP.values().stream().filter(a -> a.getParent() == null).findFirst();
        StringWriter sw = new StringWriter();
        if (root.isPresent()) {
            mapper.writeValue(sw, Collections.singletonList(root.map(Tree::new).get()));
            // In case you are running on Windows you'll need to replace double backslash with the quad for the data.js file.
            Files.write(Paths.get("html/data.js"), String.format("data='%s'", sw.toString().replace("\\", "\\\\")).getBytes());
        }
    }


}
