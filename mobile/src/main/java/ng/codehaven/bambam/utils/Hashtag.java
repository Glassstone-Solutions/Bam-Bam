package ng.codehaven.bambam.utils;

import android.content.Context;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ng.codehaven.bambam.R;

/**
 * Created by Thompson on 09/08/2015.
 */
public class Hashtag extends ClickableSpan {
    Context mContext;
    TextPaint mTextPaint;

    public Hashtag(Context ctx) {
        super();
        mContext = ctx;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        mTextPaint = ds;
        ds.setColor(ds.linkColor);
        ds.setARGB(255, 30, 144, 255);
    }

    @Override
    public void onClick(View widget) {
        TextView tv = (TextView) widget;
        Spanned s = (Spanned) tv.getText();
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);
        String theWord = s.subSequence(start + 1, end).toString();

        // Todo: This is where you replace with Intent
        Toast.makeText(mContext, String.format(mContext.getString
                (R.string.hashtag_toast_message), theWord), Toast.LENGTH_SHORT).show();

    }
}
