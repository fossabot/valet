package co.valetapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.widget.EditText;
import com.google.android.gms.maps.model.LatLng;
import com.nineoldandroids.animation.ObjectAnimator;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {
	
	TextView addressTextView, distanceTextView;
	ObjectAnimator addressAnimator, distanceAnimator;
    EditText noteEditText;
    SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        prefs = getActivity(). getSharedPreferences(Const.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.info_fragment, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		addressTextView = (TextView) view.findViewById(R.id.address_view);
		addressAnimator = ObjectAnimator.ofFloat(addressTextView, "alpha", 0f, 1f);
		addressAnimator.setDuration(500);
		
		distanceTextView = (TextView) view.findViewById(R.id.distance_view);
		distanceAnimator = ObjectAnimator.ofFloat(distanceTextView, "alpha", 0f, 1f);
		distanceAnimator.setDuration(500);

        noteEditText = (EditText) view.findViewById(R.id.noteEditText);
        noteEditText.setText(prefs.getString(Const.NOTE_KEY, ""));
        noteEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                prefs.edit().putString(Const.NOTE_KEY, v.getText().toString()).commit();

                return false;
            }
        });
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (addressAnimator.isRunning()) {
			addressAnimator.end();
		}

		if (distanceAnimator.isRunning()) {
			distanceAnimator.end();
		}
	}
	
	void clear() {
		addressTextView.setText("");
		distanceTextView.setText("");
	}
	
	class GeoCoderAsyncTask extends AsyncTask<LatLng, Void, List<Address>> {

		private Geocoder geocoder;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			geocoder = new Geocoder(getActivity());
		}

		@Override
		protected List<Address> doInBackground(LatLng... params) {

			try {
				return geocoder.getFromLocation(params[0].latitude,
						params[0].longitude, 1);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<Address> result) {
			super.onPostExecute(result);

			if (result != null && result.size() > 0) {
				addressAnimator.start();
				addressTextView.setText(result.get(0).getAddressLine(0));
			}
		}
	}
}
