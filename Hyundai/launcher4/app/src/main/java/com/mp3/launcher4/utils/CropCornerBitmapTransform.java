package com.mp3.launcher4.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * @author longzj
 */
public class CropCornerBitmapTransform extends BitmapTransformation {

    private static float radius = 0f;

    public CropCornerBitmapTransform(Context context) {
        this(context, 4);
    }

    public CropCornerBitmapTransform(Context context, int dp) {
        super(context);
        radius = ImageUtils.dp2Px(context, dp);
    }

    private static Bitmap roundCrop(BitmapPool pool, Bitmap source) {
        if (source == null) {
            return null;
        }

        Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setMaskFilter(new BlurMaskFilter(20,BlurMaskFilter.Blur.NORMAL));
        Rect rect=new Rect(0,0,canvas.getWidth(),canvas.getWidth()+20);
        RectF rectF = new RectF(0f, 0f, canvas.getWidth(), canvas.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, paint);
        canvas.drawRect(new RectF(0, radius, canvas.getWidth(), canvas.getHeight()), paint);
        canvas.drawBitmap(result.extractAlpha(paint,null),null,rect,paint);
        paint.setMaskFilter(null);
        canvas.drawBitmap(result,null,new Rect(0,0,canvas.getWidth(),canvas.getWidth()),paint);
        return result;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return roundCrop(pool, toTransform);
    }

    @Override
    public String getId() {
        return getClass().getName() + Math.round(radius);
    }
}
