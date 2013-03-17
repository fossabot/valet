package co.valetapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public class IntentLibrary {

	private IntentLibrary(){};
	
	public static Intent getFindIntent(Context context, Double latitude, Double longitude) {
		Intent intent = new Intent();

		intent.setAction(Intent.ACTION_VIEW);
		String uri = String.format("geo:%f,%f?z=%d&q=%f,%f(%s)", latitude, longitude, 
				Const.ZOOM_LEVEL, latitude, longitude, context.getString(R.string.vehicle_marker_title));


		intent.setData(Uri.parse(uri));

		return intent;
	}

	public static Intent getShareIntent(Context context, Double latitude, Double longitude) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");

		String googleMapsUrl = String.format("http://maps.google.com/?q=loc:%f,%f&z=%d",
				latitude, longitude, Const.ZOOM_LEVEL);
		intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_extra_text_parked) + " " + googleMapsUrl);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			String anchor = String.format("<a href='%s'>%s</a>",
					googleMapsUrl, context.getString(R.string.share_extra_html_text_parked_anchor));
			intent.putExtra(Intent.EXTRA_HTML_TEXT, context.getString(R.string.share_extra_html_text_parked) 
					+ " " + anchor + context.getString(R.string.period));
		}

		return intent;
	}
}
