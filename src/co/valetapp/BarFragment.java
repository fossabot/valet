package co.valetapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BarFragment extends Fragment {
	public static final String BAR_ITEMS_KEY = "co.valetapp.bar_items";

	enum BarItem {
		PARK(R.layout.park_bar_item),
		AUTO(R.layout.auto_bar_item),
        AUTO_SET(R.layout.auto_set_bar_item),
        AUTO_INFO(R.layout.auto_info_bar_item),
		LOCATING(R.layout.locating_bar_item),
		CONFIRM(R.layout.confirm_bar_item),
		RESET(R.layout.reset_bar_item), 
		FIND(R.layout.find_bar_item), 
		SHARE(R.layout.share_bar_item),
		SCHEDULE(R.layout.schedule_bar_item),
		UNSCHEDULE(R.layout.unschedule_bar_item),
		TIMER(R.layout.timer_bar_item),
		ALARM(R.layout.alarm_bar_item),
		SET(R.layout.set_bar_item),
        BLUETOOTH(R.layout.bluetooh_bar_item);

		final int mResourceId;
		BarItem(int resourceId) {
			mResourceId = resourceId;
		}

		int getResourceId() {
			return mResourceId;
		}
	}

	private LinearLayout barLinearLayout;
	private BarItem[] barItems;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.bar_fragment, null);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		barLinearLayout = (LinearLayout) view.findViewById(R.id.bar_ll);
		
		if (barItems != null) {
			setItems(barItems);
		}
	}

	void setItems(BarItem... barItems) {
		if (barLinearLayout != null) {
			for (BarItem barItem : barItems) {
				barLinearLayout.addView(getActivity().getLayoutInflater()
						.inflate(barItem.getResourceId(), barLinearLayout, false));
			}
		}
		
		this.barItems = barItems;
	}
}
