package co.valetapp;

import android.app.Application;
import com.babelsdk.main.BabelSdk;
import com.crittercism.app.Crittercism;
import com.parse.Parse;

public class Valet extends Application {
    @Override public void onCreate() {
        super.onCreate();

        BabelSdk.DEBUG = BuildConfig.DEBUG;
        BabelSdk.init(this, "valet", "", "");
        BabelSdk.setEnabled(this, true);

        Parse.initialize(this, "Rk1aoK66rLulnNtaALeL6PhQcGEDkmiudGItreof", "zcG1VzOhhxkQofbYaGNqbHC0BHKbw6myuNkZDeuq");
        Crittercism.initialize(getApplicationContext(), "5145fe5c4002050d07000002");
    }
}
