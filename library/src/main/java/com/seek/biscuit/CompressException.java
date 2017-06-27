package com.seek.biscuit;

/**
 * Created by seek on 2017/6/27.
 */

public class CompressException extends Exception {

    public String originalPath = null;

    public CompressException(String message, String path) {
        super(message);
        originalPath = path;
    }

    public CompressException(String message, String path, Throwable cause) {
        super(message, cause);
        originalPath = path;
    }
}
