package co.valetapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import co.valetapp.R;

import java.util.Calendar;

public class TimerFragment extends DynamicFragment implements OnSeekBarChangeListener {

    public static final int MAX = 48;
    public static final int INTERVAL = 15; // minutes

    TextView hoursTextView, minutesTextView;
    SeekBar timerSeekBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.timer_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hoursTextView = (TextView) view.findViewById(R.id.hour_text_view);
        minutesTextView = (TextView) view.findViewById(R.id.minutes_text_view);

        timerSeekBar = (SeekBar) view.findViewById(R.id.timer_seek_bar);
        timerSeekBar.setOnSeekBarChangeListener(this);
        timerSeekBar.setMax(MAX);
        onProgressChanged(timerSeekBar, 8, false);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (!fromUser) {
            seekBar.setProgress(progress);
        }


        String hours = Integer.toString(progress * INTERVAL / 60);
        String minutes = Integer.toString(progress * INTERVAL % 60);

        if (hours.length() == 1) hours = "0" + hours;
        if (minutes.length() == 1) minutes = "0" + minutes;

        hoursTextView.setText(hours);
        minutesTextView.setText(minutes);

    }

    public long getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hoursTextView.getText().toString()));
        calendar.add(Calendar.MINUTE, Integer.parseInt(minutesTextView.getText().toString()));

        return calendar.getTimeInMillis();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
