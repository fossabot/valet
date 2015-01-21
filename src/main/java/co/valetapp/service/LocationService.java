package co.valetapp.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import co.valetapp.util.Const;
import co.valetapp.util.Tools;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    boolean mReliablyParked;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLocation;

    Runnable mTimeout = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };
    Handler mHandler = new Handler();

    public LocationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mReliablyParked = intent.getBooleanExtra(Const.RELIABLY_PARKED_KEY, false);

        if (servicesConnected()) {
            mGoogleApiClient.connect();
            mHandler.postDelayed(mTimeout, 30 * 1000);
        } else {
            stopSelf(); // Calls onDestroy()
        }

        return START_STICKY;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(Const.TAG, "Google play connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.getAccuracy() != 0.0f && location.getAccuracy() < Const.MIN_ACCURACY) {
            stopSelf();
        }

        this.mLocation = location;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(mTimeout);
        stopLocationUpdates();

        if (mLocation != null) {
            Tools.park(this, mLocation.getLatitude(), mLocation.getLongitude(), mReliablyParked, false);
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == resultCode;
    }

    private void stopLocationUpdates() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
