package com.seek.biscuit;

import java.util.ArrayList;

/**
 * Created by seek on 2017/11/9.
 */

public class CompressResult {
    public ArrayList<String> mSuccessPaths;
    public ArrayList<String> mExceptionPaths;

    public CompressResult() {
        mSuccessPaths = new ArrayList<>();
        mExceptionPaths = new ArrayList<>();
    }
}
