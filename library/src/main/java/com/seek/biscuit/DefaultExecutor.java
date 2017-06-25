package com.seek.biscuit;

import android.os.AsyncTask;

/**
 * Created by seek on 2017/6/24.
 */

public class DefaultExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
        AsyncTask.execute(runnable);
    }
}
