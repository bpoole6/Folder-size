package com.bpoole6;

import java.util.ArrayList;
import java.util.List;

public class FolderDesc {
        private final String folder;
        private final long size;
        private final List<FolderDesc> children = new ArrayList<>();
        private FolderDesc parent;

    public FolderDesc(String folder, long size) {
        this.folder = folder;
        this.size = size;
    }

    public String getFolder() {
        return folder;
    }

    public long getSize() {
        return size;
    }

    public List<FolderDesc> getChildren() {
        return children;
    }

    public FolderDesc getParent() {
        return parent;
    }

    public void setParent(FolderDesc parent) {
        this.parent = parent;
    }
}
