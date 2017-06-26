package com.seek.biscuit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by seek on 2017/6/23.
 */

public class Utils {
    private final static String DEFAULT_IMAGE_CACHE_PATH = "biscuit_cache";
    static ArrayList<String> format = new ArrayList<>(3);
    static String JPG = ".jpg";
    static String JPEG = ".jpeg";
    static String PNG = ".png";
    static String WebP = ".webp";
    static String GIF = ".gif";
    static float REFERENCE_WIDTH = 1080f;
    static float SCALE_REFERENCE_WIDTH = 1280f;
    static float LIMITED_WIDTH = 1000f;
    static int DEFAULT_QUALITY = 66;
    static int DEFAULT_SIZE_LIMIT = 102;
    static boolean loggingEnabled = true;

    static {
        format.add(JPG);
        format.add(JPEG);
        format.add(PNG);
        format.add(WebP);
        format.add(GIF);
    }

    public static boolean isImage(String imgPath) {
        int begin = imgPath.lastIndexOf(".");
        int end = imgPath.length();
        String imageType = imgPath.substring(begin, end);
        return format.contains(imageType);
    }

    // default cache dir
    public static String getCacheDir(Context context) {
        File cacheDir = new File(context.getExternalCacheDir(), DEFAULT_IMAGE_CACHE_PATH);
        if (!cacheDir.exists()) cacheDir.mkdir();
        return cacheDir.getAbsolutePath();
    }

    // delete all cache image
    public static void clearCache(Context context) {
        clearCache(getCacheDir(context));
    }

    public static void clearCache(String dir) {
        File file = new File(dir);
        File[] files = file.listFiles();
        if (files.length > 0) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    /**
     * resolve some phone when take photo rotate some degree
     *
     * @param path
     * @return
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    // rotate to correct angle
    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

     static void log(String tag, String msg) {
        if (loggingEnabled) {
            Log.e(tag, msg);
        }
    }
}
