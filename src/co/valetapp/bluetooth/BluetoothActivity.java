package co.valetapp.bluetooth;

import co.valetapp.Const;
import co.valetapp.R;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends ListActivity {

	private BluetoothDevice[] pairedDevices = new BluetoothDevice[0];
	private BluetoothListAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_list_activity);
		pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray(pairedDevices);
		adapter =new BluetoothListAdapter();
		setListAdapter(adapter);
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Editor editor = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(Const.BLUETOOTH_KEY, adapter.getItem(position).getAddress());
		editor.commit();
		
		finish();
	}
	
	private class BluetoothListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pairedDevices.length;
		}

		@Override
		public BluetoothDevice getItem(int position) {
			return pairedDevices[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(BluetoothActivity.this).inflate(R.layout.bluetooth_row, parent, false);
			}
			
			((TextView) convertView).setText(getItem(position).getName());
			
			return convertView;
		}
	}
}
