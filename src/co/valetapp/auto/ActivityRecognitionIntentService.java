package co.valetapp.auto;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import co.valetapp.Const;
import co.valetapp.LocationService;
import co.valetapp.ParkActivity;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * User: jophde
 * Date: 5/16/13
 * Time: 10:53 PM
 */
public class ActivityRecognitionIntentService extends IntentService {

    private static final int MIN_CONFIDENCE = 60, MAX_CONFIDENCE = 100;
    private static final String LAST_ACTIVITY_TYPE_KEY = "co.valetapp.last_activity_type";
    private static final String TAG = "Auto";
    private SharedPreferences mSharedPreferences;

    public ActivityRecognitionIntentService() {
        super(ActivityRecognitionIntentService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSharedPreferences = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();

            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();


            Intent autoParkServiceIntent = new Intent(this, AutoParkService.class);
            autoParkServiceIntent.setAction(AutoParkService.ACTION_START);
            if (confidence == MAX_CONFIDENCE && activityType == DetectedActivity.STILL) {
                // Restart the service with a longer interval if the phone certain to be still
                autoParkServiceIntent.putExtra(AutoParkService.DETECTION_INTERVAL_SECONDS_KEY, 10 * 60); // 10 Minutes
            }
            else if (confidence >= MIN_CONFIDENCE) {

                int lastActivityType = mSharedPreferences.getInt(LAST_ACTIVITY_TYPE_KEY, DetectedActivity.UNKNOWN);

                if (lastActivityType == DetectedActivity.ON_BICYCLE || lastActivityType == DetectedActivity.IN_VEHICLE) {
                    autoParkServiceIntent.putExtra(AutoParkService.DETECTION_INTERVAL_SECONDS_KEY, 0);

                    if (activityType == DetectedActivity.ON_FOOT) {
                        startService(new Intent(this, LocationService.class));
                        mSharedPreferences.edit().remove(LAST_ACTIVITY_TYPE_KEY).commit();
                    }
                }

                if (activityType == DetectedActivity.ON_BICYCLE || activityType == DetectedActivity.IN_VEHICLE) {
                    mSharedPreferences.edit().putInt(LAST_ACTIVITY_TYPE_KEY, activityType).commit();
                }
            }
            startService(autoParkServiceIntent);
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
}