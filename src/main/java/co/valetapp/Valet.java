package co.valetapp;

import android.app.Application;
import com.colatris.sdk.Colatris;
import com.crittercism.app.Crittercism;
import com.taplytics.sdk.Taplytics;

public class Valet extends Application {
    @Override public void onCreate() {
        super.onCreate();

        Colatris.init(1, R.class, this, true);
        Crittercism.initialize(getApplicationContext(), "5145fe5c4002050d07000002");
//        Taplytics.startTaplytics(this, "e7a047d0103ff1d2e9af1c62d5419aa5c58b572d");
    }
}
