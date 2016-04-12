package com.quiptiq.steam;

import java.util.List;

/**
 * Node in the steam file. A node either has a value or children.
 */
public class Node {
    private final Node parent;
    private final String key;
    private final String value;
    private final List<Node> children;

    public Node(Node parent, String key, String value) {
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.children = null;
    }

    public Node(Node parent, String key, List<Node> children) {
        this.parent = parent;
        this.key = key;
        this.value = null;
        this.children = children;
    }

    public boolean hasChildren() {
        return children != null;
    }

    public String getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }
}
