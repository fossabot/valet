package co.valetapp;

import java.util.List;
import android.os.Handler;
import co.valetapp.Const;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service implements LocationListener {
	private LocationManager locationManager;
    private Location location;
    private boolean manual;

    Runnable timeout = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };

    Handler handler = new Handler();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        manual = intent.getBooleanExtra(Const.MANUAL_KEY, false);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		
		List<String> providers = locationManager.getProviders(criteria, true);
		if (providers != null) {
			for (String provider : providers) {
				locationManager.requestLocationUpdates(provider, 0, 0, this);
			}
		}

        handler.postDelayed(timeout,  30 * 1000);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location.getAccuracy() != 0.0f && location.getAccuracy() < Const.MIN_ACCURACY) {
            this.location = location;
			stopSelf(); // Calls onDestroy()
		}
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();

		locationManager.removeUpdates(this);
		handler.removeCallbacks(timeout);

        if (location != null) {
            Tools.park(this, location.getLatitude(), location.getLongitude(), manual);
        }

	}
	
	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
