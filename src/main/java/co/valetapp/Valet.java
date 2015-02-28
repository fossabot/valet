package co.valetapp;

import android.app.Application;
import android.content.Context;

import com.colatris.sdk.Colatris;
import com.crittercism.app.Crittercism;

public class Valet extends Application {

    @Override public void onCreate() {
        super.onCreate();

        Crittercism.initialize(getApplicationContext(), "5145fe5c4002050d07000002");
    }
}