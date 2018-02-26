package com.seek.biscuit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
    private boolean ignoreAlpha;
    private boolean useOriginalName;
    private long thresholdSize;
    String targetPath;
    CompressException exception;
    final Biscuit mBiscuit;

    public ImageCompressor(String path, String targetDir, int quality, @Biscuit.CompressType int compressType, boolean ignoreAlpha, boolean useOriginalName, long thresholdSize, Biscuit biscuit) {
        this.sourcePath = new ImagePath(path);
        this.targetDir = targetDir;
        this.quality = quality;
        this.compressType = compressType;
        this.ignoreAlpha = ignoreAlpha;
        this.useOriginalName = useOriginalName;
        this.thresholdSize = thresholdSize;
        this.mBiscuit = biscuit;
    }

    @Override
    public boolean compress() {
        long begin = SystemClock.elapsedRealtime();
        // check whether the original size less than thresholdSize, if less ,return the original path
        if (checkOriginalLength()) return false;
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
        Bitmap scrBitmap;
        try {
            scrBitmap = BitmapFactory.decodeFile(sourcePath.path, options);
        } catch (OutOfMemoryError outOfMemoryError) {
            generateException("no enough memory!");
            return false;
        }
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
                scrBitmap = transformBitmap(scrBitmap, matrix);
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
        FileChannel outputChannel = null;
        try {
            File targetFile = new File(targetPath);
            if (!targetFile.exists()) targetFile.createNewFile();
            outputChannel = new FileOutputStream(targetFile).getChannel();
            outputChannel.write(ByteBuffer.wrap(stream.toByteArray()));
            outputChannel.close();
            stream.close();
        } catch (IOException e) {
            String msg = "there is an exception when trying to save the compressed image!";
            log(TAG, msg);
            String path = sourcePath.path;
            exception = new CompressException(msg, path, e);
            saved = false;
        } finally {
            close(stream, outputChannel);
            long end = SystemClock.elapsedRealtime();
            long elapsed = end - begin;
            log(TAG, "the compression time is " + elapsed);
            return saved;
        }
    }

    private void close(ByteArrayOutputStream stream, FileChannel outputChannel) {
        try {
            if (stream != null) {
                stream.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkOriginalLength() {
        if (thresholdSize > 0) {
            File sourceFile = new File(sourcePath.path);
            if (!sourceFile.exists()) {
                generateException("No such file : " + sourcePath.path);
                return true;
            }
            long sourceSize = sourceFile.length();
            log(TAG, "original size : " + (sourceSize >> 10) + " KB");
            if (sourceSize <= (thresholdSize << 10)) {
                targetPath = sourcePath.path;
                return true;
            }
        }
        return false;
    }

    private void dispatchSuccess() {
        if (mBiscuit != null) {
            mBiscuit.mDispatcher.dispatchComplete(this);
        }
    }

    private void dispatchError() {
        if (mBiscuit != null) {
            mBiscuit.mDispatcher.dispatchError(this);
        }
    }

    private void generateException(String msg) {
        log(TAG, msg);
        String path = sourcePath.path;
        exception = new CompressException(msg, path);
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
        boolean success = compress();
        saveExifForResult();
        if (exception != null && !success) {
            dispatchError();
        } else {
            dispatchSuccess();
        }
    }

    private void saveExifForResult() {
        if (Utils.JPEG.contains(sourcePath.type.toLowerCase())||Utils.JPG.contains(sourcePath.type.toLowerCase())) {
            try {
                Utils.saveExif(sourcePath.path, targetPath);
            } catch (Exception e) {
                log(TAG, "can`nt save exif info!");
            }
        }
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

    //Base on the effect of Wechat (test on Density 2.75 1080*1920)
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
            int multiple = max / min;
            if (multiple < 10) {
                if (min > LIMITED_WIDTH && (1f - (ratio / 2f)) * min > LIMITED_WIDTH) {
                    scale = 1f - (ratio / 2f);
                }
            } else {
                int arg = (int) Math.pow(multiple, 2);
                scale = 1f - (arg / LIMITED_WIDTH) + (multiple > 10 ? 0.01f : 0.03f);
                if (min * scale < Utils.MIN_WIDTH) {
                    scale = 1f;
                }
            }
        }
        return scale;
    }

    private Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, false);
            if (!bitmap.sameAs(converted)) {
                bitmap = converted;
            }
        } catch (OutOfMemoryError error) {
            log(TAG, "transformBitmap: " + error);
        }
        return bitmap;
    }
}
