package co.valetapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.google.android.gms.common.GooglePlayServicesUtil;


public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about_activity);
		TextView legalNoticesTextView = (TextView) findViewById(R.id.leagal_notices_text_view);
		legalNoticesTextView.setText(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));
	}
}
