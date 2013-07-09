package co.valetapp;

import android.location.Location;
import android.os.Handler;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private boolean mReliablyParked, mManuallyParked;
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
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
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        mReliablyParked = intent.getBooleanExtra(Const.RELIABLY_PARKED_KEY, false);
        mManuallyParked = intent.getBooleanExtra(Const.MANUALLY_PARKED_KEY, false);

        if (servicesConnected()) {
            mLocationClient.connect();
            mHandler.postDelayed(mTimeout, 30 * 1000);
        } else {
            stopSelf(); // Calls onDestroy()
        }

		return START_STICKY;
	}

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        stopSelf();
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
            Tools.park(this, mLocation.getLatitude(), mLocation.getLongitude(), mReliablyParked, mManuallyParked);
        }
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    private void stopLocationUpdates() {
        if (servicesConnected()) {
            if (mLocationClient.isConnected()) {
                mLocationClient.removeLocationUpdates(this);
                mLocationClient.disconnect();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
