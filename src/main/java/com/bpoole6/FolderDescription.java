package com.bpoole6;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds infomation about a folder.
 */
public class FolderDescription {
        private final String folder;
        private final long size;
        private final List<FolderDescription> children = new ArrayList<>();
        private FolderDescription parent;

    public FolderDescription(String folder, long size) {
        this.folder = folder;
        this.size = size;
    }

    public String getFolder() { return folder; }

    public long getSize() { return size; }

    public List<FolderDescription> getChildren() { return children; }

    public FolderDescription getParent() { return parent; }

    public void setParent(FolderDescription parent) { this.parent = parent; }
}
