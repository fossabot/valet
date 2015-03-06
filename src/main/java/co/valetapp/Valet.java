package co.valetapp;

import android.app.Application;
import android.content.Context;

import com.colatris.sdk.Colatris;
import com.crittercism.app.Crittercism;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Valet extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
    }
}