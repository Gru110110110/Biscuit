package com.seek.biscuit;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.seek.biscuit.Utils.log;

/**
 * Created by seek on 2017/6/23.
 */

public class Biscuit {
    private final static String TAG = "Biscuit";
    public final static int SCALE = 0;
    public final static int SAMPLE = 1;
    Dispatcher mDispatcher;
    Executor mExecutor;
    String targetDir;
    boolean ignoreAlpha;
    int quality;
    int compressType;
    boolean useOriginalName;
    long thresholdSize;
    ArrayList<CompressListener> mCompressListeners;
    ArrayList<String> mPaths;

    Biscuit(ArrayList<String> paths, String targetDir, boolean ignoreAlpha, int quality, int compressType, boolean useOriginalName, boolean loggingEnabled, long thresholdSize, CompressListener compressListener, Executor executor) {
        Utils.loggingEnabled = loggingEnabled;
        mDispatcher = new Dispatcher();
        mExecutor = executor;
        mCompressListeners = new ArrayList<>();
        addListener(compressListener);
        mPaths = new ArrayList<>();
        mPaths.addAll(paths);
        this.targetDir = targetDir;
        this.ignoreAlpha = ignoreAlpha;
        this.quality = quality;
        this.compressType = compressType;
        this.useOriginalName = useOriginalName;
        this.thresholdSize = thresholdSize;
        compress();
    }

    public void compress() {
        Iterator<String> iterator = mPaths.iterator();
        while (iterator.hasNext()) {
            String path = iterator.next();
            if (Utils.isImage(path)) {
                Compressor compressor = new ImageCompressor(path, targetDir, quality, compressType, ignoreAlpha, useOriginalName, thresholdSize, this);
                mExecutor.execute(compressor);
            } else {
                log(TAG, "can not recognize the path : " + path);
            }
            iterator.remove();
        }
    }

    public void addListener(CompressListener compressListener) {
        mCompressListeners.add(compressListener);
    }

    public void removeListener(CompressListener compressListener) {
        mCompressListeners.remove(compressListener);
    }

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public boolean isIgnoreAlpha() {
        return ignoreAlpha;
    }

    public void setIgnoreAlpha(boolean ignoreAlpha) {
        this.ignoreAlpha = ignoreAlpha;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        }
        this.quality = quality;
    }

    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(@CompressType int compressType) {
        this.compressType = compressType;
    }

    public boolean isUseOriginalName() {
        return useOriginalName;
    }

    public void setUseOriginalName(boolean useOriginalName) {
        this.useOriginalName = useOriginalName;
    }

    public long getThresholdSize() {
        return thresholdSize;
    }

    public void setThresholdSize(long thresholdSize) {
        this.thresholdSize = thresholdSize;
    }

    public ArrayList<String> getPaths() {
        return mPaths;
    }

    public void addPaths(ArrayList<String> paths) {
        if (paths != null && paths.size() > 0) {
            mPaths.addAll(paths);
        }
    }

    public void addPaths(String path) {
        if (!TextUtils.isEmpty(path)) {
            mPaths.add(path);
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    //default
    public static void clearCache(Context context) {
        Utils.clearCache(context);
    }

    //if have been customize cache dir
    public static void clearCache(String dir) {
        Utils.clearCache(dir);
    }

    public void dispatchSuccess(String targetPath) {
        for (CompressListener compressListener :
                mCompressListeners) {
            compressListener.onSuccess(targetPath);
        }
    }

    public void dispatchError(CompressException exception) {
        for (CompressListener compressListener :
                mCompressListeners) {
            compressListener.onError(exception);
        }
    }

    @IntDef({SAMPLE, SCALE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CompressType {
    }

    public static class Builder {
        private ArrayList<String> mPaths;
        private String mTargetDir;
        private boolean mIgnoreAlpha;
        private int mQuality;
        private int mCompressType;
        private boolean mUseOriginalName;
        private CompressListener mCompressListener;
        private Context mContext;
        private Executor mExecutor;
        private boolean loggingEnabled;
        private long mThresholdSize;

        public Builder(Context context) {
            this.mContext = context.getApplicationContext();
            mQuality = Utils.getDefaultQuality(context);
            mPaths = new ArrayList<>();
            mCompressType = SCALE;
            mIgnoreAlpha = false;
            mUseOriginalName = false;
            loggingEnabled = true;
            mThresholdSize = -1;
        }

        public Builder targetDir(String targetDir) {
            if (!TextUtils.isEmpty(targetDir)) {
                String last = targetDir.substring(targetDir.length() - 1, targetDir.length());
                if (!last.equals("/")) {
                    throw new IllegalArgumentException("targetDir must be end with \"/\"");
                }
            }
            mTargetDir = targetDir;
            return this;
        }

        public Builder ignoreAlpha(boolean ignoreAlpha) {
            mIgnoreAlpha = ignoreAlpha;
            return this;
        }

        /**
         * Note that the unit is KB
         */
        public Builder ignoreLessThan(long thresholdSize) {
            mThresholdSize = thresholdSize;
            return this;
        }

        public Builder originalName(boolean originalName) {
            mUseOriginalName = originalName;
            return this;
        }

        public Builder compressType(@CompressType int compressType) {
            mCompressType = compressType;
            return this;
        }

        public Builder loggingEnabled(boolean enabled) {
            loggingEnabled = enabled;
            return this;
        }

        public Builder executor(Executor executor) {
            mExecutor = executor;
            return this;
        }

        public Builder quality(int quality) {
            if (quality < 0 || quality > 100) {
                throw new IllegalArgumentException("quality must be 0..100");
            }
            mQuality = quality;
            return this;
        }

        public Builder listener(CompressListener compressListener) {
            mCompressListener = compressListener;
            return this;
        }

        public Builder path(String source) {
            mPaths.add(source);
            return this;
        }

        public Builder path(List<String> source) {
            mPaths.addAll(source);
            return this;
        }

        public Biscuit build() {
            if (TextUtils.isEmpty(mTargetDir)) {
                mTargetDir = Utils.getCacheDir(mContext);
            }
            if (mExecutor == null) {
                mExecutor = new DefaultExecutor();
            }
            return new Biscuit(mPaths, mTargetDir, mIgnoreAlpha, mQuality, mCompressType, mUseOriginalName, loggingEnabled, mThresholdSize, mCompressListener, mExecutor);
        }
    }

}
