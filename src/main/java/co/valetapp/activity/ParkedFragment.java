package co.valetapp.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.*;
import co.valetapp.R;
import co.valetapp.util.Const;
import co.valetapp.util.Tools;
import com.nineoldandroids.animation.ObjectAnimator;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ParkedFragment extends DynamicFragment implements View.OnLongClickListener {

    public TextView addressTextView, distanceTextView;
    public ObjectAnimator addressAnimator, distanceAnimator;
    EditText noteEditText;
    public ImageButton cameraImageButton, deletePictureButton;
    SharedPreferences prefs;
    View root;
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    LinearLayout countdownLinearLayout;
    ObjectAnimator countdownAnimator;
    TextView hoursTextView, minutesTextView, secondsTextView, dateTextView;
    long time;
    Ringtone ringtone;
    CountDownTimer countDownTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.parked_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        root = view.findViewById(R.id.root);

        cameraImageButton = (ImageButton) view.findViewById(R.id.cameraImageButton);
        cameraImageButton.setOnLongClickListener(this);

        deletePictureButton = (ImageButton) view.findViewById(R.id.deletePictureButton);
        cameraImageButton.setOnLongClickListener(this);

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

        Uri ringtoneUri;
        if (prefs.contains(Const.RINGTONE_URI_KEY)) {
            ringtoneUri = Uri.parse(prefs.getString(Const.RINGTONE_URI_KEY, null));
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }

        ringtone = RingtoneManager.getRingtone(getActivity().getApplicationContext(), ringtoneUri);

        countdownLinearLayout = (LinearLayout) view.findViewById(R.id.countdown_linear_layout);
        hoursTextView = (TextView) view.findViewById(R.id.hours_text_view);
        minutesTextView = (TextView) view.findViewById(R.id.minutes_text_view);
        secondsTextView = (TextView) view.findViewById(R.id.seconds_text_view);
        dateTextView = (TextView) view.findViewById(R.id.dateTextView);

        countdownAnimator = ObjectAnimator.ofFloat(countdownLinearLayout, "alpha", 1f, 0f, 1f);
        countdownAnimator.setRepeatCount(Animation.INFINITE);
        countdownAnimator.setRepeatMode(ObjectAnimator.INFINITE);
        countdownAnimator.setDuration(500);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (prefs.contains(Const.IMAGE_KEY) && Tools.hasExternalStoragePublicPicture()) {
            deletePictureButton.setVisibility(View.VISIBLE);
            cameraImageButton.setImageResource(R.drawable.picture_selector);
            cameraImageButton.setContentDescription(getString(R.string.view));
        } else {
            deletePictureButton.setVisibility(View.INVISIBLE);
            cameraImageButton.setImageResource(R.drawable.camera_selector);
            cameraImageButton.setContentDescription(getString(R.string.camera_button_cd));
        }

        if (Tools.isTimed(getActivity())) {
            countdownLinearLayout.setVisibility(View.VISIBLE);

            long timeInMillis = Tools.getTime(getActivity());

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date(timeInMillis));
            dateTextView.setText(currentDateTimeString);

            long millisInFuture = timeInMillis - System.currentTimeMillis();

            countDownTimer = new CountDownTimer(millisInFuture, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {


                    String hours = null, minutes = null, seconds = null;

                    if (millisUntilFinished >= HOUR) {
                        hours = Long.toString(millisUntilFinished / HOUR);
                        millisUntilFinished %= HOUR;
                    } else {
                        hours = "0";
                    }

                    if (millisUntilFinished >= MINUTE) {
                        minutes = Long.toString(millisUntilFinished / MINUTE);
                        if (minutes.length() == 1) minutes = "0" + minutes;
                        millisUntilFinished %= MINUTE;
                    } else {
                        minutes = "00";
                    }

                    if (millisUntilFinished > SECOND) {
                        seconds = Long.toString(millisUntilFinished / SECOND);
                        if (seconds.length() == 1) seconds = "0" + seconds;
                        millisUntilFinished %= SECOND;
                    } else {
                        seconds = "00";
                    }

                    secondsTextView.setText(seconds);
                    minutesTextView.setText(minutes);
                    hoursTextView.setText(hours);

                    time = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    alarm();
                }
            }.start();
        } else {
            countdownLinearLayout.setVisibility(View.GONE);
        }
    }

    private void alarm() {
        if (prefs.getBoolean(Const.ALARM_KEY, false)) {
            if (ringtone != null) {
                ringtone.play();
            }
        }

        hoursTextView.setText("00");
        minutesTextView.setText("00");
        secondsTextView.setText("00");

        countdownAnimator.start();
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

        if (countdownAnimator.isRunning()) {
            countdownAnimator.end();
        }

        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(getActivity(), v.getContentDescription(), Toast.LENGTH_LONG).show();

        return true;
    }

    public class GeoCoderAsyncTask extends AsyncTask<Location, Void, List<Address>> {

        private Geocoder geocoder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            geocoder = new Geocoder(getActivity());
        }

        @Override
        protected List<Address> doInBackground(Location... params) {

            try {
                return geocoder.getFromLocation(params[0].getLatitude(),
                        params[0].getLongitude(), 1);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Address> result) {
            super.onPostExecute(result);

            if (!isVisible()) return;

            if (result != null && result.size() > 0) {
                CharSequence t = addressTextView.getText();
                String s = t == null ? "" : t.toString();
                String addressLine = result.get(0).getAddressLine(0);
                if (!addressLine.equals(s)) {
                    addressAnimator.start();
                    addressTextView.setText(addressLine);
                }
            } else {
                addressTextView.setText(getString(R.string.no_address));
            }
        }
    }
}
