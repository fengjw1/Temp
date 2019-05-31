package com.mp3.launcher4.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
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
    private Typeface mTypeSemiBold;
    private Typeface mTypeMuliRegular;

    private FontUtils(Context context) {
        AssetManager assetManager = context.getAssets();
        mTypeBold = Typeface.createFromAsset(assetManager, "fonts/fonts/Nunito-Bold.ttf");
        mTypExtraLight = Typeface.createFromAsset(assetManager, "fonts/fonts/Nunito-ExtraLight.ttf");
        mTypeLight = Typeface.createFromAsset(assetManager, "fonts/fonts/Nunito-Light.ttf");
        mTypeRegular = Typeface.createFromAsset(assetManager, "fonts/fonts/Nunito-Regular.ttf");
        mTypeSemiBold = Typeface.createFromAsset(assetManager, "fonts/fonts/Nunito-SemiBold.ttf");
        mTypeMuliRegular = Typeface.createFromAsset(assetManager, "fonts/fonts/Muli-Regular.ttf");
    }

    public static FontUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FontUtils(context);
        }
        return mInstance;
    }

    public void setBoldFont(TextView textView) {
        textView.setTypeface(mTypeBold);
    }

    public void setExtraLightFont(TextView textView) {
        textView.setTypeface(mTypExtraLight);
    }

    public void setLightFont(TextView textView) {
        textView.setTypeface(mTypeLight);
    }

    public void setRegularFont(TextView textView) {
        textView.setTypeface(mTypeRegular);
    }

    public void setSemiBoldFont(TextView textView) {
        textView.setTypeface(mTypeSemiBold);
    }

    public  void setMuliRegularFont(TextView textView){textView.setTypeface(mTypeMuliRegular);}
}
