package com.example.fixture;

import android.util.Log;

public class Logger {
    private final String tag;

    public Logger(String tag) {
        this.tag = tag;
    }

    public void info(String message) {
        Log.i(tag, message);
    }
}
