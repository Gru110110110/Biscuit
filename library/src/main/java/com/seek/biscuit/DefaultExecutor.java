package com.seek.biscuit;

import android.os.AsyncTask;

/**
 * Created by seek on 2017/6/27.
 */

public class DefaultExecutor implements Executor {
    @Override
    public void execute(Runnable compressor) {
        AsyncTask.SERIAL_EXECUTOR.execute(compressor);
    }
}
