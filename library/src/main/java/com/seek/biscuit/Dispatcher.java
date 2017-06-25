package com.seek.biscuit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by seek on 2017/6/23.
 */

public class Dispatcher {

    private final Handler mHandler;
    private static final int MESSAGE_COMPLETE = 0x1;
    private static final int MESSAGE_ERROR = 0x2;

    public Dispatcher() {
        mHandler = new DispatchHandler(Looper.getMainLooper());
    }

    void dispatchComplete(ImageCompressor compressor) {
        Message message = mHandler.obtainMessage(MESSAGE_COMPLETE, compressor);
        message.sendToTarget();
    }

    void dispatchError(ImageCompressor compressor) {
        Message message = mHandler.obtainMessage(MESSAGE_ERROR, compressor);
        message.sendToTarget();
    }

    private static class DispatchHandler extends Handler {
        public DispatchHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            ImageCompressor compressor = (ImageCompressor) msg.obj;
            switch (msg.what) {
                case MESSAGE_COMPLETE:
                    compressor.compressListener.onSuccess(compressor.targetPath);
                    break;
                case MESSAGE_ERROR:
                    compressor.compressListener.onError(compressor.exception);
                    break;
            }
        }
    }
}
