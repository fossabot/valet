package co.valetapp;

import android.app.Application;
import com.colatris.sdk.Colatris;
import com.crittercism.app.Crittercism;

public class Valet extends Application {
    @Override public void onCreate() {
        super.onCreate();

        Colatris.init(1, R.class, this, BuildConfig.DEBUG);
        Crittercism.initialize(getApplicationContext(), "5145fe5c4002050d07000002");
    }
}
