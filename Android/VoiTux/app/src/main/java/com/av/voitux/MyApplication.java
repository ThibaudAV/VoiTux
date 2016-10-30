package com.av.voitux;

import android.app.Application;
import android.content.res.Configuration;

import com.av.voitux.Models.ConnexionsManager;

/**
 * Created by thibaud on 26/09/16.
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
