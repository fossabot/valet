package co.valetapp.view;

import co.valetapp.R;
import co.valetapp.view.util.UiUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class FontableCheckedTextView extends CheckedTextView {

	public FontableCheckedTextView(Context context) {
		super(context);
	}

	public FontableCheckedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		UiUtil.setCustomFont(this, context, attrs,  
				R.styleable.com_roqbot_client_view_FontableCheckedTextView,
				R.styleable.com_roqbot_client_view_FontableCheckedTextView_font);
	}
	
	public FontableCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UiUtil.setCustomFont(this,context,attrs,
                R.styleable.com_roqbot_client_view_FontableCheckedTextView,
                R.styleable.com_roqbot_client_view_FontableCheckedTextView_font);
    }

}
