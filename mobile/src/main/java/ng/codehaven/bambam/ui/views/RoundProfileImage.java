package ng.codehaven.bambam.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.facebook.login.widget.ProfilePictureView;


public class RoundProfileImage extends ProfilePictureView {


    private BezelImageView image;

    /**
     * Constructor
     *
     * @param context Context for this View
     */
    public RoundProfileImage(Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context Context for this View
     * @param attrs   AttributeSet for this View.
     */
    public RoundProfileImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     *
     * @param context  Context for this View
     * @param attrs    AttributeSet for this View.
     *                 The attribute 'preset_size' is processed here
     * @param defStyle Default style for this View
     */
    public RoundProfileImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context c) {
        removeAllViews();

        image = new BezelImageView(c);

        LayoutParams imageLayout = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        image.setLayoutParams(imageLayout);
        // We want to prevent up-scaling the image, but still have it fit within
        // the layout bounds as best as possible.
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        addView(image);
    }

}
