package com.ktc.ecuador.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import com.mstar.android.tv.TvPictureManager;
import com.mstar.android.tvapi.common.PictureManager;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.EnumScalerWindow;
import com.mstar.android.tvapi.common.vo.VideoWindowType;


/**
 * @author longzj
 */
public class ImageUtils {

    private final static float[] BT_SELECTED = new float[]{1, 0, 0, 0, 5, 0, 1, 0, 0, 5, 0, 0, 1, 0, 5, 0, 0, 0, 1, 0};
    private final static int HD_WIDTH = 1920;
    private final static int HD_HEIGHT = 1080;
    private final static float HD_DENSITY = 1.5f;
    private static float screenDensity = 1.0f;

    private static int windowWidth;
    private static int windowHeight;

    /**
     * 过滤时使图像置灰
     *
     * @param imageView 图像
     * @param isFilter  是否过滤
     */
    public static void setImageFilter(ImageView imageView, boolean isFilter) {
        if (!isFilter) {
            imageView.setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
        } else {
            imageView.setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void removeImageDrawable(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }


    /**
     * 获取屏幕的屏幕密度（在第一次启动launcher时调用）
     *
     * @param manager 窗体管理器
     */
    public static void setScreenDensity(WindowManager manager) {
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        windowWidth = metrics.widthPixels;
        windowHeight = metrics.heightPixels;
        screenDensity = metrics.density;
    }

    /**
     * 设置电视全屏
     */
    public static void fullScreen() {
        int panelWidth = getScreenWidth();
        int panelHeight = getScreenHeight();
        scaleWindow(0, 0, panelWidth, panelHeight, true);
    }

    public static int getScreenWidth() {
        TvPictureManager manager = TvPictureManager.getInstance();
        return manager.getPanelWidthHeight().width;
    }

    public static int getScreenHeight() {
        TvPictureManager manager = TvPictureManager.getInstance();
        return manager.getPanelWidthHeight().height;
    }

    /**
     * 缩放电视
     *
     * @param x            缩放点左上角x坐标
     * @param y            缩放点左上角y坐标
     * @param width        缩放的宽度
     * @param height       缩放的高度
     * @param isFullScreen 是否全屏
     */
    static void scaleWindow(int x, int y, int width, int height, boolean isFullScreen) {
        if (!isFullScreen) {
            float[] ratios = getScreenRatio();
            x = (int) (x * ratios[0]);
            y = (int) (y * ratios[1]);
            width = (int) (width * ratios[0]);
            height = (int) (height * ratios[1]);
        }

        VideoWindowType videoWindowType = new VideoWindowType();
        videoWindowType.x = x;
        videoWindowType.y = y;
        videoWindowType.width = width;
        videoWindowType.height = height;

        PictureManager pictureManager = TvManager.getInstance().getPictureManager();
        try {
            pictureManager.selectWindow(EnumScalerWindow.E_MAIN_WINDOW);
            pictureManager.setDisplayWindow(videoWindowType);
            pictureManager.scaleWindow();
        } catch (TvCommonException e) {
            e.printStackTrace();
        }
    }

    /**
     * 以高清尺寸为标准，计算当前设备物理宽高与高清的比例
     *
     * @return 返回当前设备以高清为标准的宽高比
     */
    private static float[] getScreenRatio() {
        return new float[]{
                getScreenXRatio(), getScreenYRatio()
        };
    }

    private static float getScreenXRatio() {
        return getScreenWidth() * HD_DENSITY / (HD_WIDTH * screenDensity);
    }

    private static float getScreenYRatio() {
        return getScreenHeight() * HD_DENSITY / (HD_HEIGHT * screenDensity);
    }

    public static int dp2Px(Context context, double dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dp2Px(Context context, int dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dp2Px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getWindowHeight() {
        return windowHeight;
    }

    public static int getWindowWidth() {
        return windowWidth;
    }
}
