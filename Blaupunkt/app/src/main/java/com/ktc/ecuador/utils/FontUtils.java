package com.ktc.ecuador.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * @author longzj
 */
public class FontUtils {

    private static FontUtils mInstance;
    private Typeface mTypeBold;
    private Typeface mTypExtraLight;
    private Typeface mTypeLight;
    private Typeface mTypeRegular;
    private Typeface mTypeMedium;

    private FontUtils(Context context) {
        AssetManager assetManager = context.getAssets();
        mTypeLight = Typeface.createFromAsset(assetManager, "fonts/Rubik-Light.ttf");
        mTypeRegular = Typeface.createFromAsset(assetManager, "fonts/Rubik-Regular.ttf");
        mTypeMedium = Typeface.createFromAsset(assetManager, "fonts/Rubik-Medium.ttf");
    }

    public static FontUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FontUtils(context);
        }
        return mInstance;
    }


    public void setLightFont(TextView textView) {
        textView.setTypeface(mTypeLight);
    }

    public void setRegularFont(TextView textView) {
        textView.setTypeface(mTypeRegular);
    }

    public void setMediumFont(TextView textView) {
        textView.setTypeface(mTypeMedium);
    }
}
