package co.valetapp;

import java.util.Calendar;
import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AlarmFragment extends DynamicFragment {

	Spinner dayOfWeekSpinner, hourSpinner, minuteSpinner, amPmSpinner;
    boolean is24HourClock;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.alarm_fragment, null);
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

		adapter = ArrayAdapter.createFromResource(getActivity(), R.array.hour, R.layout.spinner_item);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		hourSpinner.setAdapter(adapter);

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
		switch(calendar.get(Calendar.DAY_OF_WEEK)) {
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

        if (getResources().getConfiguration().locale.equals(Locale.US)) {
            is24HourClock = false;
        }
        else {
            amPmSpinner.setVisibility(View.GONE);
            is24HourClock = true;
        }
	}

	long getTime() {



        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MINUTE, getMinute());

        if (is24HourClock) {
            calendar.set(Calendar.HOUR_OF_DAY, getHour());
        }
        else {
            calendar.set(Calendar.HOUR, getHour());
            calendar.set(Calendar.AM_PM, getAmPm());
        }

        calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek());

		if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
			calendar.add(Calendar.WEEK_OF_YEAR, 1);
		}

		return calendar.getTimeInMillis();
	}


	int getDayOfWeek() {
		switch(dayOfWeekSpinner.getSelectedItemPosition()) {
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
		return Integer.parseInt((String) hourSpinner.getSelectedItem());
	}

	int getMinute() {
		return Integer.parseInt((String) minuteSpinner.getSelectedItem());
	}

	int getAmPm() {
		switch(amPmSpinner.getSelectedItemPosition()) {
		case 0:
			return Calendar.AM;
		case 1:
			return Calendar.PM;
		default:
			return 0;
		}
	}
}
