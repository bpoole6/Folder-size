package com.bpoole6;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This data structure is the expected structure for the javascript library when serialized into json.
 */
public  class Tree {
        private String label;
        private List<Tree> children;

        public Tree(FolderDescription folderDescription) {
            String folderName = Paths.get(folderDescription.getFolder()).toString();
            String name;
            if (folderDescription.getSize() < 1000) {
                name = String.format("%s (>1KB)", folderName);
            } else {
                name = String.format("%s (%.2fMB)", folderName, (folderDescription.getSize() / (double) (1024 * 1024)));
            }
            this.label = name;
            this.children = folderDescription.getChildren().stream().sorted(Comparator.comparingLong((FolderDescription a) -> a.getSize()).reversed()).map(Tree::new).collect(Collectors.toList());
        }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public List<Tree> getChildren() { return children; }

    public void setChildren(List<Tree> children) { this.children = children; }
}
