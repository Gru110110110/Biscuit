package com.seek.sample;

import android.os.Environment;

import java.io.File;

public class FileUtils {

    private final static String DEFAULT_APK_PATH = "/Biscuit/";
    private final static String DEFAULT_IMAGE_PATH = "image/";

    public static String getImageDir() {
        String root = getAppPath() + DEFAULT_IMAGE_PATH;
        File file = new File(root);
        if (!file.exists()) file.mkdir();
        return root;
    }

    /**
     * get app root path
     *
     * @return
     */
    public static String getAppPath() {
        String path = null;
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            path = Environment.getExternalStorageDirectory() + DEFAULT_APK_PATH;
        }
        File file = new File(path);
        if (!file.exists()) file.mkdir();
        return path;
    }

}
