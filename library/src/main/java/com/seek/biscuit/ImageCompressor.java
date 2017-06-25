package com.seek.biscuit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.seek.biscuit.Utils.LIMITED_WIDTH;
import static com.seek.biscuit.Utils.SCALE_REFERENCE_WIDTH;
import static com.seek.biscuit.Utils.log;

/**
 * Created by seek on 2017/6/23.
 */

public class ImageCompressor implements Compressor {
    private final static String TAG = "ImageCompressor";
    private ImagePath sourcePath;
    private String targetDir;
    private int quality;
    private int compressType;
    private Dispatcher dispatcher;
    private boolean ignoreAlpha;
    private boolean useOriginalName;
    CompressListener compressListener;
    String targetPath;
    Exception exception;

    public ImageCompressor(String path, String targetDir, int quality, @Biscuit.CompressType int compressType, boolean ignoreAlpha, boolean useOriginalName, Dispatcher dispatcher, CompressListener compressListener) {
        this.sourcePath = new ImagePath(path);
        this.targetDir = targetDir;
        this.quality = quality;
        this.compressType = compressType;
        this.dispatcher = dispatcher;
        this.compressListener = compressListener;
        this.ignoreAlpha = ignoreAlpha;
        this.useOriginalName = useOriginalName;
    }

    @Override
    public boolean compress() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourcePath.path, options);
        if (options.outHeight <= 0 || options.outWidth <= 0) {
            generateException("an error occurs when trying to decode!");
            return false;
        }
        boolean compressBySample = compressType == Biscuit.SAMPLE;
        int inSampleSize = 1;
        if (compressBySample) {
            inSampleSize = calculateInSampleSize(options);
            options.inSampleSize = inSampleSize;
        }
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = ignoreAlpha ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888;
        boolean permit = memoryEnough(options.outWidth / inSampleSize, options.outHeight / inSampleSize);
        if (!permit) {
            generateException("no enough memory!");
            return false;
        }
        Bitmap scrBitmap = BitmapFactory.decodeFile(sourcePath.path, options);
        if (scrBitmap == null) {
            generateException("the image data could not be decoded!");
            return false;
        }
        if (!compressBySample) {
            float scale = calculateScaleSize(options);
            log(TAG, "scale : " + scale);
            if (scale != 1f) {
                Matrix matrix = new Matrix();
                matrix.setScale(scale, scale);
                scrBitmap = Bitmap.createBitmap(scrBitmap, 0, 0, scrBitmap.getWidth(), scrBitmap.getHeight(), matrix, false);
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean success = scrBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        scrBitmap.recycle();
        if (!success) {
            generateException("unsuccessfully compressed to the specified stream!");
            return false;
        }
        targetPath = getCacheFileName();
        log(TAG, "the image data will be saved at " + targetPath);
        boolean saved = true;
        try {
            File targetFile = new File(targetPath);
            if (!targetFile.exists()) targetFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(targetFile);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (IOException e) {
            log(TAG, "there is an exception when trying to save the compressed image!");
            exception = e;
            saved = false;
        } finally {
            if (exception != null && !saved) {
                dispatchError();
            } else {
                dispatchSuccess();
            }
            return saved;
        }
    }

    private void dispatchSuccess() {
        if (dispatcher != null) {
            dispatcher.dispatchComplete(this);
        }
    }

    private void dispatchError() {
        if (dispatcher != null) {
            dispatcher.dispatchError(this);
        }
    }

    private void generateException(String msg) {
        log(TAG, msg);
        exception = new IllegalArgumentException(msg);
        dispatchError();
    }

    private boolean memoryEnough(int width, int height) {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();
        int allocation = (width * height) << (ignoreAlpha ? 1 : 2);
        log(TAG, "free : " + free / 1024 / 1024 + "MB, need : " + allocation / 1024 / 1024 + "MB");
        return allocation < free;
    }

    private String getCacheFileName() {
        StringBuilder cacheBuilder = new StringBuilder();
        cacheBuilder.append(targetDir);
        if (useOriginalName && !TextUtils.isEmpty(sourcePath.name)) {
            cacheBuilder.append(sourcePath.name);
        } else {
            cacheBuilder.append("biscuitCache").append(System.currentTimeMillis());
        }
        cacheBuilder.append(TextUtils.isEmpty(sourcePath.type) ? Utils.JPG : sourcePath.type);
        return cacheBuilder.toString();
    }

    @Override
    public void run() {
        compress();
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        int inSampleSize = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        if (width < height) {
            inSampleSize = getInSampleSize(inSampleSize, width);
        } else {
            inSampleSize = getInSampleSize(inSampleSize, height);
        }
        return inSampleSize;
    }

    private int getInSampleSize(int inSampleSize, int width) {
        float ratio = width / Utils.REFERENCE_WIDTH;
        if (ratio > 1.5f && ratio <= 3) {
            inSampleSize = inSampleSize << 1;
        } else if (ratio > 3) {
            inSampleSize = inSampleSize << 2;
        }
        return inSampleSize;
    }

    private float calculateScaleSize(BitmapFactory.Options options) {
        float scale = 1f;
        int width = options.outWidth;
        int height = options.outHeight;
        int max = Math.max(width, height);
        int min = Math.min(width, height);
        float ratio = min / (max * 1f);
        if (ratio >= 0.5f) {
            if (max > SCALE_REFERENCE_WIDTH) scale = SCALE_REFERENCE_WIDTH / (max * 1f);
        } else {
            if (min > LIMITED_WIDTH && (1 - (ratio / 2)) * min > LIMITED_WIDTH) {
                scale = 1 - (ratio / 2);
            }
        }
        return scale;
    }
}
