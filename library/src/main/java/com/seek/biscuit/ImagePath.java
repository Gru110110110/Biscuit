package com.seek.biscuit;

/**
 * Created by seek on 2017/6/23.
 */

public class ImagePath {
    String path;
    String type;
    String name;

    public ImagePath(String path) {
        this.path = path;
        int typeSplit = path.lastIndexOf(".");
        int nameSplit = path.lastIndexOf("/");
        if (typeSplit != -1 && nameSplit != -1) {
            this.name = path.substring(nameSplit + 1, typeSplit);
        }
        if (typeSplit != -1) {
            this.type = path.substring(typeSplit, path.length());
        }
    }
}
