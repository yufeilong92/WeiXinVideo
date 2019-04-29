package com.example.myapplication;

import android.app.Application;
import android.content.Context;

public class CameraApplication extends Application {
    public static Context sApplication;
    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        sApplication = getApplicationContext();
    }
}
