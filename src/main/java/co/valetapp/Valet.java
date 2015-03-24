package co.valetapp;

import android.app.Application;

import com.colatris.sdk.Colatris;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class Valet extends Application {

    @Override public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        Colatris.setDebug(BuildConfig.DEBUG);
    }
}