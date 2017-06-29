package com.seek.biscuit;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

/**
 * Created by seek on 2017/6/24.
 */

public class HandlerExecutor implements Executor {
    private final static String TAG = "HandlerExecutor";
    private final CompressHandler mCompressHandler;

    public HandlerExecutor() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mCompressHandler = new CompressHandler(thread.getLooper());
    }

    @Override
    public void execute(Runnable runnable) {
        Message message = mCompressHandler.obtainMessage();
        message.obj = runnable;
        message.sendToTarget();
    }

    private static class CompressHandler extends Handler {
        public CompressHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Runnable runnable = (Runnable) msg.obj;
            runnable.run();
        }
    }
}
