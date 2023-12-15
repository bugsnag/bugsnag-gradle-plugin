package com.example.fixture;

import android.os.Bundle;
import android.app.Activity;
public class FixtureActivity extends Activity {
    private Logger logger;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger = new Logger("FixtureActivity");
        logger.info("onCreate()");
        logger.info(stringFromJNI());
    }

    public void onStart() {
        super.onStart();
        logger.info("onStart()");
    }

    public void onStop() {
        super.onStop();
        logger.info("onStop()");
    }

    public native String stringFromJNI();
}

