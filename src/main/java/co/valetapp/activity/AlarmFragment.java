package co.valetapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import co.valetapp.R;
import co.valetapp.util.Const;
import co.valetapp.util.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmFragment extends DynamicFragment {

    Spinner dayOfWeekSpinner, hourSpinner, minuteSpinner, amPmSpinner;
    boolean is24HourClock;
    private final static List<CharSequence> HOURS_LIST_24, HOURS_LIST_12;
    static {
        HOURS_LIST_24 = new ArrayList<>();
        for (int i = 0; i < 24; i++) HOURS_LIST_24.add(Integer.toString(i));

        HOURS_LIST_12 = new ArrayList<>();
        for (int i = 1; i <= 12; i++) HOURS_LIST_12.add(Integer.toString(i));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return getActivity().getLayoutInflater().inflate(R.layout.alarm_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dayOfWeekSpinner = (Spinner) view.findViewById(R.id.day_of_week_spinner);
        hourSpinner = (Spinner) view.findViewById(R.id.hour_spinner);
        minuteSpinner = (Spinner) view.findViewById(R.id.minute_spinner);
        amPmSpinner = (Spinner) view.findViewById(R.id.am_pm_spinner);

        ArrayAdapter<CharSequence>
                adapter = ArrayAdapter.createFromResource(getActivity(), R.array.day_of_week, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        dayOfWeekSpinner.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.minute, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        minuteSpinner.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(getActivity(), R.array.am_pm, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        amPmSpinner.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR, 10);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.AM_PM, Calendar.AM);

        int position = 0;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                position = 0;
                break;
            case Calendar.MONDAY:
                position = 1;
                break;
            case Calendar.TUESDAY:
                position = 2;
                break;
            case Calendar.WEDNESDAY:
                position = 3;
                break;
            case Calendar.THURSDAY:
                position = 4;
                break;
            case Calendar.FRIDAY:
                position = 5;
                break;
            case Calendar.SATURDAY:
                position = 6;
                break;
        }
        dayOfWeekSpinner.setSelection(position);

        hourSpinner.setSelection(9);
        minuteSpinner.setSelection(0);
        amPmSpinner.setSelection(0);
    }

    @Override public void onStart() {
        super.onStart();

        is24HourClock = Tools.getPrefs(getActivity()).getBoolean(Const.IS_24_HOUR_CLOCK, false);

        ArrayAdapter<CharSequence> adapter;
        if (is24HourClock) {
            adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, HOURS_LIST_24);
        } else {
            adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, HOURS_LIST_12);
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        hourSpinner.setAdapter(adapter);

        if (is24HourClock) {
            amPmSpinner.setVisibility(View.GONE);
        }
    }

    public long getTime() {


        Calendar calendar = Calendar.getInstance();

        if (is24HourClock) {
            calendar.set(Calendar.HOUR_OF_DAY, getHour());
        } else {
            calendar.set(Calendar.HOUR, getHour());
            calendar.set(Calendar.AM_PM, getAmPm());
        }

        calendar.set(Calendar.MINUTE, getMinute());

        calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek());

        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }


    int getDayOfWeek() {
        switch (dayOfWeekSpinner.getSelectedItemPosition()) {
            case 0:
                return Calendar.SUNDAY;
            case 1:
                return Calendar.MONDAY;
            case 2:
                return Calendar.TUESDAY;
            case 3:
                return Calendar.WEDNESDAY;
            case 4:
                return Calendar.THURSDAY;
            case 5:
                return Calendar.FRIDAY;
            case 6:
                return Calendar.SATURDAY;
            default:
                return 0;
        }
    }

    int getHour() {
        int i =  Integer.parseInt((String) hourSpinner.getSelectedItem());
        if (!is24HourClock) {
            if (i == 12) {
                return 0;
            } else {
                return i;
            }
        } else {
            return i;
        }
    }

    int getMinute() {
        return Integer.parseInt((String) minuteSpinner.getSelectedItem());
    }

    int getAmPm() {
        switch (amPmSpinner.getSelectedItemPosition()) {
            case 0:
                return Calendar.AM;
            case 1:
                return Calendar.PM;
            default:
                return 0;
        }
    }
}
