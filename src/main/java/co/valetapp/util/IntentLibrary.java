package co.valetapp.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;

import java.util.Locale;

import co.valetapp.R;

public class IntentLibrary {

    private IntentLibrary() {
    }

    public static Intent getFindIntent(Context context) {
        Intent intent = new Intent();

        SharedPreferences prefs = Tools.getPrefs(context);
        String latitude = prefs.getString(Const.LAT_KEY, "");
        String longitude = prefs.getString(Const.LONG_KEY, "");

        intent.setAction(Intent.ACTION_VIEW);
        String uri = String.format(Locale.US, "geo:%s,%s?z=%d&q=%s,%s", latitude, longitude,
                Const.ZOOM_LEVEL, latitude, longitude);


        intent.setData(Uri.parse(uri));

        return intent;
    }

    public static Intent getShareIntent(Context context, Double latitude, Double longitude) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String googleMapsUrl;
        if (Tools.isTimed(context)) {
            googleMapsUrl = String.format(Locale.US, "http://maps.google.com/?q=loc:%f,%f&z=%d&t=%s",
                    latitude, longitude, Const.ZOOM_LEVEL, Long.toString(Tools.getTime(context)));
        } else {
            googleMapsUrl = String.format(Locale.US, "http://maps.google.com/?q=loc:%f,%f&z=%d",
                    latitude, longitude, Const.ZOOM_LEVEL);
        }

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
