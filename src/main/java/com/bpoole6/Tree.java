package com.bpoole6;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public  class Tree {
        private String label;
        private List<Tree> children;

        public Tree(FolderDesc folderDesc) {
            String folderName = Paths.get(folderDesc.getFolder()).toString();
            String name;
            if (folderDesc.getSize() < 1000) {
                name = String.format("%s (>1KB)", folderName);
            } else {
                name = String.format("%s (%.2fMB)", folderName, (folderDesc.getSize() / (double) (1024 * 1024)));
            }
            this.label = name;
            this.children = folderDesc.getChildren().stream().sorted(Comparator.comparingLong((FolderDesc a) -> a.getSize()).reversed()).map(Tree::new).collect(Collectors.toList());
        }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public void setChildren(List<Tree> children) {
        this.children = children;
    }
}
