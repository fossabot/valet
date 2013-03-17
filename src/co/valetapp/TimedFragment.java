package co.valetapp;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;

public class TimedFragment extends DynamicFragment {
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;

	LinearLayout countdownLinearLayout;
	ObjectAnimator countdownAnimator;
	TextView hoursTextView, minutesTextView, secondsTextView;
	long time;
    Ringtone ringtone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.timed_fragment, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone  = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);

		countdownLinearLayout = (LinearLayout) view.findViewById(R.id.countdown_linear_layout);
		hoursTextView = (TextView) view.findViewById(R.id.hours_text_view);
		minutesTextView = (TextView) view.findViewById(R.id.minutes_text_view);
		secondsTextView = (TextView) view.findViewById(R.id.seconds_text_view);
		
		countdownAnimator = ObjectAnimator.ofFloat(countdownLinearLayout, "alpha", 1f, 0f, 1f);
		countdownAnimator.setRepeatCount(Animation.INFINITE);
		countdownAnimator.setRepeatMode(ObjectAnimator.INFINITE);
		countdownAnimator.setDuration(500);
	}

	@Override
	public void onResume() {
		super.onResume();
		long millisInFuture = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
				.getLong(Const.TIME_KEY, 0) - System.currentTimeMillis();
		
		if (millisInFuture < 0) {
			hoursTextView.setText("00");
			minutesTextView.setText("00");
			secondsTextView.setText("00");
			countdownAnimator.start();

            ringtone.play();
		}
		else {
			new CountDownTimer(millisInFuture, 1000) {
				
				@Override
				public void onTick(long millisUntilFinished) {
					
					
					String hours = null, minutes = null, seconds = null;
					
					if (millisUntilFinished >= HOUR) {
						hours = Long.toString(millisUntilFinished / HOUR);
						if (hours.length() == 1) hours = "0" + hours;
						millisUntilFinished %= HOUR;
					}
                    else {
                        hours = "00";
                    }
					
					if (millisUntilFinished >= MINUTE) {
						minutes = Long.toString(millisUntilFinished / MINUTE);
						if (minutes.length() == 1) minutes = "0" + minutes;
						millisUntilFinished %= MINUTE;
					}
                    else {
                        minutes = "00";
                    }
					
					if (millisUntilFinished > SECOND) {
						seconds = Long.toString(millisUntilFinished / SECOND);
						if (seconds.length() == 1) seconds = "0" + seconds;
						millisUntilFinished %= SECOND;
					}
                    else {
                        seconds = "00";
                    }
					
					secondsTextView.setText(seconds);
					minutesTextView.setText(minutes);
					hoursTextView.setText(hours);
					
					time = millisUntilFinished;
				}

				@Override
				public void onFinish() {
					hoursTextView.setText("00");
					minutesTextView.setText("00");
					secondsTextView.setText("00");
					
					countdownAnimator.start();

                    ringtone.play();
				}
			}.start();
		}
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
	}
}
