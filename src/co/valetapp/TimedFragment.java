package co.valetapp;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.*;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nineoldandroids.animation.ObjectAnimator;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimedFragment extends DynamicFragment {
    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final int DAY = 24 * HOUR;
    LinearLayout countdownLinearLayout;
    ObjectAnimator countdownAnimator;
    TextView hoursTextView, minutesTextView, secondsTextView, dateTextView;
    long time;
    Ringtone ringtone;
    PowerManager.WakeLock wl;
    CountDownTimer countDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.timed_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);

        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Valet");

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

        long timeInMillis = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).getLong(Const.TIME_KEY, 0);

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date(timeInMillis));
        dateTextView.setText(currentDateTimeString);

        long millisInFuture = timeInMillis - System.currentTimeMillis();

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {


                String hours = null, minutes = null, seconds = null;

                if (millisUntilFinished >= HOUR) {
                    hours = Long.toString(millisUntilFinished / HOUR);
//                    if (hours.length() == 1) hours = "0" + hours;
                    millisUntilFinished %= HOUR;
                } else {
                    hours = "00";
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
    }

    private void alarm() {
        String action = getActivity().getIntent().getAction();
        if (action != null && action.equals(Const.ACTION_ALARM)) {
            ((ParkActivity) getActivity()).setState(ParkActivity.State.UNSCHEDULE);
            ringtone.play();
        }

        // TODO don't make the alarm ring twice if the user is viewing the TimedFragment already

        hoursTextView.setText("00");
        minutesTextView.setText("00");
        secondsTextView.setText("00");

        countdownAnimator.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (countdownAnimator.isRunning()) {
            countdownAnimator.end();
        }

        if (ringtone.isPlaying()) {
            ringtone.stop();
        }

        countDownTimer.cancel();
        countDownTimer = null;
    }
}
