package com.seek.biscuit;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seek on 2017/6/23.
 */

public class Biscuit {

    public final static int SCALE = 0;
    public final static int SAMPLE = 1;

    Biscuit(ArrayList<String> paths, String targetDir, boolean ignoreAlpha, int quality, int compressType, boolean useOriginalName, boolean loggingEnabled, CompressListener compressListener, Executor executor) {
        Utils.loggingEnabled = loggingEnabled;
        Dispatcher dispatcher = new Dispatcher();
        for (String path : paths) {
            Compressor compressor = new ImageCompressor(path, targetDir, quality, compressType, ignoreAlpha, useOriginalName, dispatcher, compressListener);
            executor.execute(compressor);
        }
    }

    public static Builder with(Context context) {
        return new Builder(context);
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

        public Builder(Context context) {
            this.mContext = context.getApplicationContext();
            mQuality = Utils.DEFAULT_QUALITY;
            mPaths = new ArrayList<>();
            mCompressType = SCALE;
            mIgnoreAlpha = false;
            mUseOriginalName = false;
            loggingEnabled = true;
        }

        public Builder targetDir(String targetDir) {
            mTargetDir = targetDir;
            return this;
        }

        public Builder ignoreAlpha(boolean ignoreAlpha) {
            mIgnoreAlpha = ignoreAlpha;
            return this;
        }

        public Builder originalName(boolean originalName) {
            mUseOriginalName = originalName;
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
            return new Biscuit(mPaths, mTargetDir, mIgnoreAlpha, mQuality, mCompressType, mUseOriginalName, loggingEnabled, mCompressListener, mExecutor);
        }
    }
}
