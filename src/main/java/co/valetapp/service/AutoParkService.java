package co.valetapp.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationServices;

import co.valetapp.util.Const;

/**
 * User: jophde
 * Date: 5/16/13
 * Time: 10:24 PM
 */
public class AutoParkService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Auto";

    public static final String ACTION_START = "co.valetapp.start";
    public static final String ACTION_STOP = "co.valetapp.stop";
    public static final String DETECTION_INTERVAL_SECONDS_KEY = "co.valetapp.interval"; // Seconds
    // Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DEFAULT_DETECTION_INTERVAL_SECONDS = 20;

    public static int DETECTION_INTERVAL_MILLISECONDS;

    /*
     * Store the PendingIntent used to send activity recognition events
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private GoogleApiClient googleApiClient;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private REQUEST_TYPE mRequestType;

    @Override
    public void onCreate() {
        super.onCreate();

        mInProgress = false;

        /*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(this, ActivityRecognitionIntentService.class);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            DETECTION_INTERVAL_MILLISECONDS =
                    intent.getIntExtra(DETECTION_INTERVAL_SECONDS_KEY, DEFAULT_DETECTION_INTERVAL_SECONDS)
                            * MILLISECONDS_PER_SECOND;

            int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (statusCode != ConnectionResult.SUCCESS) {
                stopSelf();
            } else {
                if (intent.getAction().equals(ACTION_START)) {
                    startUpdates();
                } else if (intent.getAction().equals(ACTION_STOP)) {
                    stopUpdates();
                }

            }
        }

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Request activity recognition updates based on the current
     * detection interval.
     */
    public void startUpdates() {
        mRequestType = REQUEST_TYPE.START;

        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            googleApiClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
            googleApiClient.disconnect();
            mInProgress = false;
            startUpdates();
        }
    }

    /**
     * Turn off activity recognition updates
     */
    public void stopUpdates() {
        mRequestType = REQUEST_TYPE.STOP;
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            googleApiClient.connect();
            //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
            googleApiClient.disconnect();
            mInProgress = false;
            stopUpdates();
        }
    }

    /*
    * Called by Location Services once the location client is connected.
    *
    * Continue by requesting activity updates.
    */
    @Override
    public void onConnected(Bundle bundle) {
        switch (mRequestType) {
            case START:

        /*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */

         ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(googleApiClient, DETECTION_INTERVAL_MILLISECONDS,
                 mActivityRecognitionPendingIntent);

        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
                mInProgress = false;
                googleApiClient.disconnect();
                break;

            case STOP:
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(googleApiClient, mActivityRecognitionPendingIntent);

                mInProgress = false;
                googleApiClient.disconnect();

                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Const.TAG, "Connection to Google Play suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Turn off the request flag
        mInProgress = false;
    }

    public enum REQUEST_TYPE {START, STOP}
}
